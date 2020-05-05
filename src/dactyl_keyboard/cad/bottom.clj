;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Bottom Plating                                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.bottom
  (:require [scad-clj.model :as model]
            [scad-klupe.iso :as threaded]
            [scad-tarmi.core :refer [π] :as tarmi-core]
            [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.misc :refer [colours]]
            [dactyl-keyboard.cad.central :as central]
            [dactyl-keyboard.cad.misc :as misc :refer [merge-bolt wafer]]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.cad.body :as body]
            [dactyl-keyboard.cad.tweak :as tweak]
            [dactyl-keyboard.cad.wrist :as wrist]
            [dactyl-keyboard.cad.auxf :as auxf]
            [dactyl-keyboard.param.access :refer [most-specific compensator]]))


;;;;;;;;;;;
;; Basic ;;
;;;;;;;;;;;

(defn anchor-positive-nonprojecting
  "A shape that holds a screw, and possibly a heat-set insert.
  Written for use as an OpenSCAD module.
  This model is a short sequence of seamlessly connecting conical frusta
  with an optional spherical cap, otherwise a bevelled top edge.
  One of the concerns here is to keep the edges of the model fairly straight,
  both to make it easy to connect to case walls of different shapes and
  to make sure the projection of the model onto z = 0 is an accurate footprint.
  The only way the side of the model won’t be straight is if the user
  configuration specifies heat-set inserts of uneven diameter.
  Precautions against a paradoxical user configuration are minimal."
  [getopt]
  (let [prop (partial getopt :main-body :bottom-plate :installation)
        ins (partial prop :inserts)
        bolt-prop (prop :fasteners :bolt-properties)
        m-diameter (:m-diameter bolt-prop)
        z-head (threaded/head-length m-diameter :countersunk)
        z-end (max (if (ins :include) (+ z-head (ins :length)) 0)
                   (threaded/bolt-length bolt-prop))
        core (if (ins :include) [(ins :diameter :top) (ins :diameter :bottom)]
                                [m-diameter])
        shell-radii (mapv (fn [d] (+ (/ d 2) (prop :thickness))) (distinct core))
        ;; Ignore z-head unless we have two different radii.
        base (partition 2 (interleave shell-radii [z-end z-head]))
        [top-radius top-height] (first base)]
    (maybe/union
      (when (prop :dome-caps)
        (model/translate [0 0 z-end] (model/sphere top-radius)))
      (apply maybe/union
        (map
          (fn [[[r1 h1] [r0 h0]]]
            (let [length (- h1 h0)
                  thickness (if (= r0 r1) r0 [r0 r1])]
              (when (pos? length)  ; Omit contradictions, redundancies.
                (maybe/translate [0 0 (+ h0 (/ length 2))]
                  (model/cylinder thickness length)))))
          (->>
            (concat  ; Compile a list of circle radii and heights.
              (if (prop :dome-caps)
                [(first base)]  ; No bevelling.
                [[(dec top-radius) top-height]  ; Bevelling.
                 [top-radius (dec top-height)]])
              (rest base)
              ;; Finally a bottom segment, from the level of the
              ;; bottom plate to the floor. Right-angled profile.
              [[(last shell-radii) (getopt :main-body :bottom-plate :thickness)]
               [(last shell-radii) 0]])
            (remove nil?)
            (distinct)  ; In case the screw head stops at plate level.
            (partition 2 1)))))))  ; Connecting pairs make frusta.

(defn- max-anchor-thickness
  "The maximum width of a screw anchor for attaching a bottom plate."
  [getopt]
  (let [prop (partial getopt :main-body :bottom-plate :installation)
        ins (partial prop :inserts)]
    (+ (/ (if (ins :include) (max (ins :diameter :top) (ins :diameter :bottom))
                             (prop :fasteners :bolt-properties :m-diameter))
          2)
       (prop :thickness))))

(defn anchor-positive-central
  "A possibly extended version of anchor-positive-nonprojecting.
  If projections are enabled for the central housing, this module
  refers twice to the result of anchor-positive-nonprojecting above
  via its module. Otherwise, it is just one reference, as a sort of
  redirection."
  [getopt]
  (let [base "bottom_plate_anchor_positive_nonprojecting"]
    (maybe/hull
      (model/call-module base)
      (when (getopt :central-housing :bottom-plate :projections :include)
        (->> (model/call-module base)
          ;; Get a 2D cut through the middle of the ordinary anchor, assuming
          ;; it’s rotationally symmetric in the x-y plane.
          (model/rotate [(/ π 2) 0 0])
          (model/project)
          ;; Scale the 2D cut and extrude to 3D as a thin sliver.
          (model/scale
            (getopt :central-housing :bottom-plate :projections :scale))
          (model/extrude-linear {:height wafer})
          ;; Rotate back up to stand beside the original.
          (model/rotate [(/ π -2) 0 0])
          ;; Move to the edge of the thickest part of the original.
          (model/translate [0 (max-anchor-thickness getopt) 0]))))))

(defn screw-negative
  "The shape of a screw. Threading is disabled for inserts.
  Written for use as an OpenSCAD module."
  [getopt]
  (let [prop (partial getopt :main-body :bottom-plate :installation)
        bolt-prop (prop :fasteners :bolt-properties)]
    (model/rotate [π 0 0]
      (merge-bolt
       {:compensator (compensator getopt), :negative true}
       bolt-prop
       (when (prop :inserts :include) {:include-threading false})))))

(defn insert-negative
  "The shape of a heat-set insert for a screw."
  [getopt]
  (let [base (partial getopt :main-body :bottom-plate :installation)
        prop (partial base :inserts)
        {:keys [m-diameter head-type]} (base :fasteners :bolt-properties)
        head (threaded/head-length m-diameter head-type)
        thickness (getopt :main-body :bottom-plate :thickness)
        gap (max 0 (- thickness head))]
    (maybe/union
      (model/translate [0 0 (+ thickness gap (/ (prop :length) 2))]
        (model/cylinder [(/ (prop :diameter :bottom) 2)
                         (/ (prop :diameter :top) 2)]
                        (prop :length)))
      (when-not (zero? gap)
        ;; The head of the screw is longer than the plate is thick.
        ;; Leave empty space between the plate and the insert.
        (model/translate [0 0 (+ thickness (/ gap 2))]
          ;; A bottom-hull on the previous shape would widen the hole in the
          ;; bottom plate (in a preview) and prevent the use of a bottom
          ;; diameter smaller than the top (admittedly strange).
          (model/cylinder (/ (prop :diameter :bottom) 2) gap))))))

(defn- to-3d
  "Build a 3D bottom plate from a 2D block."
  [getopt block]
  (model/extrude-linear
    {:height (getopt :main-body :bottom-plate :thickness), :center false}
    block))


;;;;;;;;;;;;;;;
;; Fasteners ;;
;;;;;;;;;;;;;;;

;; The proper selection of fasteners varies with the program output.
;; For example, as long as the bottom plate for the main body also
;; covers half of the central housing, the screw holes for the bottom
;; plate must include positions from two sources.

(defn- all-fastener-positions
  "Collate the various sources of fastener positions for filtering.
  Return a vector of fastener positions where each position is annotated
  with a locally namespaced keyword corresponding to its source."
  [getopt]
  (mapcat
    (fn [[source-type raw-path]]
      (map #(assoc % ::type source-type) (apply getopt raw-path)))
    (concat
      ;; Be sensitive to the relevant inclusion switches, so that the
      ;; higher-level model functions don’t always need to be.
      (when (getopt :main-body :bottom-plate :include)
        [[::main
          [:main-body :bottom-plate :installation :fasteners :positions]]])
      (when (and (getopt :main-body :bottom-plate :include)
                 (getopt :central-housing :derived :include-main))
        [[::centre
          [:central-housing :bottom-plate :fastener-positions]]])
      (when (getopt :wrist-rest :bottom-plate :include)
        [[::wrist
          [:wrist-rest :bottom-plate :fastener-positions]]]))))

(let [module-names {1 "bottom_plate_anchor_positive_nonprojecting"
                    2 "bottom_plate_anchor_positive_central"
                    3 "bottom_plate_screw_negative"
                    4 "bottom_plate_insert_negative"}]
  (defn- fastener-fn
    "Place instances of a predefined module according to user configuration.
    The passed predicate function is used to select positions, while the
    OpenSCAD module is identified by an integer key, for brevity."
    ([type-id]
     (fastener-fn type-id some?))
    ([type-id pred]
     (fastener-fn type-id pred false))
    ([type-id pred offset-z]
     (fastener-fn type-id pred offset-z false))
    ([type-id pred offset-z mirror]
     (fn [getopt]
       (let [z-offset (getopt :dfm :bottom-plate :fastener-plate-offset)
             module (get module-names type-id)]
         (maybe/translate [0 0 (if offset-z z-offset 0)]
           (apply maybe/union
             (map (place/module-z0-2d-placer getopt module mirror)
                  (filter pred (all-fastener-positions getopt))))))))))

(defn- any-type
  "Return a predicate function for filtering fasteners.
  The filter will match fastener positions of any type included in the whitelist
  passed to this function."
  [& types]
  (fn [position] (some (set types) #{(::type position)})))

(def posts-in-main-body (fastener-fn 1 (any-type ::main)))
(def posts-for-main-plate (fastener-fn 1 (any-type ::main ::centre)))
(def posts-in-wrist-rest (fastener-fn 1 (any-type ::wrist)))
(def posts-in-central-housing (fastener-fn 2 (any-type ::centre)))
(def holes-in-main-body (fastener-fn 3 (any-type ::main ::centre)))
(def holes-in-main-plate (fastener-fn 3 (any-type ::main ::centre) true))
(def holes-in-left-housing-body (fastener-fn 3 (any-type ::centre) false true))
(def holes-in-left-housing-plate (fastener-fn 3 (any-type ::centre) true true))
(def holes-in-wrist-body (fastener-fn 3 (any-type ::wrist)))
(def holes-in-wrist-plate (fastener-fn 3 (any-type ::wrist) true))
(def inserts (fastener-fn 4))


;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn- mask-3d
  "A shape akin to the body mask but restricted to bottom-plate height."
  [getopt]
  (let [[x y _] (getopt :mask :size)
        z (getopt :main-body :bottom-plate :thickness)]
    (maybe/translate [0 0 (/ z 2)]
      (model/cube x y z))))

(defn- mask-2d
  "A 2D mask. This takes the central housing into account, for restricting that
  feature to the centre line."
  [getopt]
  (let [[x y _] (getopt :mask :size)]
    (if (and (getopt :central-housing :derived :include-main))
      (maybe/translate [(/ x 4) 0]
        (model/square (/ x 2) y))
      (model/square x y))))

(defn- masked-cut
  "A slice of a 3D object at z=0, restricted by the mask, not hulled."
  [getopt shape]
  (when-not (empty? shape)
    (->> shape model/cut (model/intersection (mask-2d getopt)))))

(defn- wall-base-3d
  "A sliver cut from the case wall."
  [getopt]
  (model/intersection
    (mask-3d getopt)
    (maybe/union
      (key/metacluster body/cluster-wall getopt)
      (tweak/plating getopt true :main-body))))

(defn- floor-finder
  "Make a function that takes a key mount and returns a 2D vertex
  (xy-coordinate pair) for the exterior wall of the case extending from
  that key mount."
  [getopt cluster]
  (fn [[coord direction turning-fn]]
    (let [key [:wall (compass/short-to-long direction) :extent]
          extent (most-specific getopt key cluster coord)
          side (compass/tuple-to-intermediate
                   [direction (turning-fn direction)])]
      (when (= extent :full)  ; Ignore partial walls.
        (take 2 (place/wall-corner-place getopt cluster coord
                  {:side side, :segment 3, :vertex true}))))))

(defn- cluster-floor-polygon
  "A polygon approximating a floor-level projection of a key clusters’s wall."
  [getopt cluster]
  (maybe/polygon
    (filter some?  ; Get rid of edges with partial walls.
      (mapcat identity  ; Flatten floor-finder output by one level only.
        (reduce
          (fn [coll position]
            (conj coll
              (map (floor-finder getopt cluster)
                   (body/connecting-wall position))))
          []
          (key/walk-cluster getopt cluster))))))

(defn- check-chousing-polygon
  [points]
  (when (< (count (distinct points)) 2)
    (throw (ex-info (str "Too few points at ground level in central housing. "
                         "Unable to draw bottom plate.")
             {:available-points points
              :minimum-count 2}))))

(defn- chousing-floor-polygon
  "A polygon for the body of the central housing
  This goes through all interface points at ground and then reverses through
  the most extreme of those points, shifted to x=0."
  [getopt]
  (let [gabel (central/interface getopt :at-ground [:points :at-ground :gabel])
        centre-full (sort (map #(assoc % 0 0) gabel))]
    (check-chousing-polygon gabel)
    (model/polygon (concat gabel [(last centre-full) (first centre-full)]))))

(defn- chousing-adapter-polygon
  "A polygon for the central-housing adapter."
  [getopt]
  (let [gabel (central/interface getopt :at-ground [:points :at-ground :gabel])
        adapter (central/interface getopt :at-ground [:points :at-ground :adapter])]
    (check-chousing-polygon gabel)
    (model/polygon (concat gabel (reverse adapter)))))

(defn- rhousing-floor-polygon
  "A polygon describing the area underneath the rear housing.
  A projection of the 3D shape would work but it would require taking care to
  hull the straight and other parts of the housing separately, because the
  connection between them may be concave. The 2D approach is safer."
  [getopt]
  (model/polygon (map (fn [pillar-fn] (take 2 (pillar-fn true false)))
                      (body/rhousing-pillar-functions getopt))))

(defn- case-positive-2d
  "A union of polygons representing the interior of the case, including the
  central housing, when configured to appear."
  ;; Built for maintainability rather than rendering speed.
  [getopt]
  (maybe/union
    (key/metacluster cluster-floor-polygon getopt)
    (masked-cut getopt (posts-for-main-plate getopt))
    (tweak/floor-polygons getopt)
    (when (getopt :central-housing :derived :include-main)
      (chousing-floor-polygon getopt))
    (when (getopt :central-housing :derived :include-adapter)
      (chousing-adapter-polygon getopt))
    ;; With a rear housing that connects to a regular key cluster wall, there
    ;; is a distinct possibility that two polygons (one for the housing, one
    ;; for the cluster wall) will overlap at one vertex, forming a union where
    ;; that particular intersection has no thickness.
    ;; As of 2019-04-9, OpenSCAD’s development snapshots will render a linear
    ;; extrusion from such a shape perfectly and it will produce a valid STL
    ;; that a slicer can handle.
    ;; However, if you then order some Boolean interaction between the
    ;; extrusion and any other 3D shape, while OpenSCAD will still render it,
    ;; it will not slice correctly: The mesh will be broken at the
    ;; intersection.
    (when (getopt :main-body :rear-housing :include)
      ;; To work around the problem, the rear housing floor polygon is moved a
      ;; tiny bit toward the origin, preventing the vertex overlap.
      (model/translate [0 (- wafer)] (rhousing-floor-polygon getopt)))
    (when (and (getopt :wrist-rest :include)
               (= (getopt :wrist-rest :style) :threaded))
      (model/cut (wrist/all-case-blocks getopt)))
    (maybe/cut (auxf/ports-positive getopt #{:main-body :central-housing}))))

(defn case-positive
  "A model of a bottom plate for the entire case but not the wrist rests.
  Screw holes not included."
  [getopt]
  (model/union
    (wall-base-3d getopt)
    (to-3d getopt (case-positive-2d getopt))))

(defn case-complete
  "A printable model of a case bottom plate in one piece."
  [getopt]
  (model/color (:bottom-plate colours)
    (maybe/difference
      (case-positive getopt)
      (holes-in-main-plate getopt)
      (holes-in-left-housing-plate getopt))))


;;;;;;;;;;;;;;;;;
;; Wrist Rests ;;
;;;;;;;;;;;;;;;;;

(defn- wrist-positive-2d [getopt]
  (maybe/union
    (model/cut (wrist/projection-maquette getopt))
    (model/cut (posts-in-wrist-rest getopt))))

(defn wrist-positive
  "3D wrist-rest bottom plate without screw holes."
  [getopt]
  (to-3d getopt (wrist-positive-2d getopt)))

(defn wrist-complete
  "A printable model of a wrist-rest bottom plate in one piece."
  [getopt]
  (model/color (:bottom-plate colours)
    (maybe/difference
      (wrist-positive getopt)
      (holes-in-wrist-plate getopt))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Combined Case and Wrist-Rest Plates ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn combined-positive
  "A combined bottom plate for case and wrist rest.
  This assumes the use of a threaded-style wrist rest, but will obviously
  prevent the use of threaded fasteners in adjusting the wrist rest.
  This is therefore recommended only where there is no space available between
  case and wrist rest."
  [getopt]
  (model/union
    (wall-base-3d getopt)
    (to-3d getopt
      (model/union
        (case-positive-2d getopt)
        (apply maybe/union
          (reduce
            (fn [coll pair]
              (conj coll (apply model/hull (map model/cut pair))))
            []
            (wrist/block-pairs getopt)))
        (wrist-positive-2d getopt)))))

(defn combined-complete
  [getopt]
  (model/color (:bottom-plate colours)
    (maybe/difference
      (combined-positive getopt)
      (holes-in-main-plate getopt)
      (holes-in-wrist-plate getopt))))
