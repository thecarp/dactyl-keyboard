;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Bottom Plating                                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.bottom
  (:require [clojure.spec.alpha :as spec]
            [scad-clj.model :as model]
            [scad-klupe.iso :as threaded]
            [scad-tarmi.core :refer [π] :as tarmi-core]
            [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.misc :refer [colours]]
            [dactyl-keyboard.cad.central :as central]
            [dactyl-keyboard.cad.misc :as misc :refer [merge-bolt wafer]]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.cad.body :as body]
            [dactyl-keyboard.cad.wrist :as wrist]
            [dactyl-keyboard.param.access
             :refer [most-specific get-key-alias main-body-tweak-data]]))


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
  (let [prop (partial getopt :case :bottom-plate :installation)
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
              [[(last shell-radii) (getopt :case :bottom-plate :thickness)]
               [(last shell-radii) 0]])
            (remove nil?)
            (distinct)  ; In case the screw head stops at plate level.
            (partition 2 1)))))))  ; Connecting pairs make frusta.

(defn- max-anchor-thickness
  "The maximum width of a screw anchor for attaching a bottom plate."
  [getopt]
  (let [prop (partial getopt :case :bottom-plate :installation)
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
      (when (getopt :case :central-housing :bottom-plate :projections :include)
        (->> (model/call-module base)
          ;; Get a 2D cut through the middle of the ordinary anchor, assuming
          ;; it’s rotationally symmetric in the x-y plane.
          (model/rotate [(/ π 2) 0 0])
          (model/project)
          ;; Scale the 2D cut and extrude to 3D as a thin sliver.
          (model/scale
            (getopt :case :central-housing :bottom-plate :projections :scale))
          (model/extrude-linear {:height wafer})
          ;; Rotate back up to stand beside the original.
          (model/rotate [(/ π -2) 0 0])
          ;; Move to the edge of the thickest part of the original.
          (model/translate [0 (max-anchor-thickness getopt) 0]))))))

(defn anchor-negative
  "The shape of a screw and optionally a heat-set insert for that screw.
  Written for use as an OpenSCAD module."
  [getopt]
  (let [prop (partial getopt :case :bottom-plate :installation)
        bolt-prop (prop :fasteners :bolt-properties)
        head-type (prop :fasteners :bolt-properties :head-type)]
    (maybe/union
      (model/rotate [π 0 0]
        (merge-bolt
          {:compensator (getopt :dfm :derived :compensator), :negative true}
          bolt-prop))
      (when (prop :inserts :include)
        (let [d0 (prop :inserts :diameter :bottom)
              d1 (prop :inserts :diameter :top)
              z0 (threaded/head-length (:m-diameter bolt-prop) head-type)
              z1 (+ z0 (prop :inserts :length))]
          (misc/bottom-hull (model/translate [0 0 z1]
                              (model/cylinder (/ d1 2) wafer))
                            (model/translate [0 0 z0]
                              (model/cylinder (/ d0 2) wafer))))))))

(defn- to-3d
  "Build a 3D bottom plate from a 2D block."
  [getopt block]
  (model/extrude-linear
    {:height (getopt :case :bottom-plate :thickness), :center false}
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
      (when (getopt :case :bottom-plate :include)
        [[::main
          [:case :bottom-plate :installation :fasteners :positions]]])
      (when (and (getopt :case :bottom-plate :include)
                 (getopt :case :central-housing :derived :include-main))
        [[::centre
          [:case :central-housing :bottom-plate :fastener-positions]]])
      (when (getopt :wrist-rest :bottom-plate :include)
        [[::wrist
          [:wrist-rest :bottom-plate :fastener-positions]]]))))

(let [module-names {1 "bottom_plate_anchor_positive_nonprojecting"
                    2 "bottom_plate_anchor_positive_central"
                    3 "bottom_plate_anchor_negative"}]
  (defn- fasteners
    "Place instances of a predefined module according to user configuration.
    The passed predicate function is used to select positions, while the
    OpenSCAD module is identified by an integer key, for brevity."
    ([getopt pred type-id]
     (fasteners getopt pred type-id false))
    ([getopt pred type-id mirror]
     (apply maybe/union
       (map (place/module-z0-2d-placer getopt (get module-names type-id) mirror)
            (filter pred (all-fastener-positions getopt)))))))

(defn- any-type
  "Return a predicate function for filtering fasteners.
  The filter will match fastener positions of any type included in the whitelist
  passed to this function."
  [& types]
  (fn [position] (some (set types) #{(::type position)})))

(def anchors-in-main-body #(fasteners % (any-type ::main) 1))
(def anchors-in-central-housing #(fasteners % (any-type ::centre) 2))
(def anchors-for-main-plate #(fasteners % (any-type ::main ::centre) 1))
(def anchors-in-wrist-rest #(fasteners % (any-type ::wrist) 1))
(def holes-in-main-plate #(fasteners % (any-type ::main ::centre) 3))
(def holes-in-left-housing #(fasteners % (any-type ::centre) 3 true))
(def holes-in-wrist-plate #(fasteners % (any-type ::wrist) 3))
(def holes-in-combo #(fasteners % (any-type ::main ::wrist) 3))


;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn- mask-3d
  "A shape akin to the body mask but restricted to bottom-plate height."
  [getopt]
  (let [[x y _] (getopt :mask :size)
        z (getopt :case :bottom-plate :thickness)]
    (maybe/translate [0 0 (/ z 2)]
      (model/cube x y z))))

(defn- mask-2d
  "A 2D mask. This takes the central housing into account, for restricting that
  feature to the centre line."
  [getopt]
  (let [[x y _] (getopt :mask :size)]
    (if (and (getopt :case :central-housing :derived :include-main))
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
      (body/main-body-tweaks getopt))))

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
                  {:side side, :vertex true}))))))

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
          (matrix/trace-between
            (getopt :key-clusters :derived :by-cluster cluster :key-requested?)))))))

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
  (model/polygon
    (reduce
      (fn [coll pillar-fn] (conj coll (take 2 (pillar-fn true false))))
      []
      (body/rhousing-pillar-functions getopt))))

(defn- tweak-floor-vertex
  "A corner vertex on a tweak wall, extending from a key mount."
  [getopt segment-picker bottom [alias side first-segment last-segment]]
  {:post [(spec/valid? ::tarmi-core/point-2d %)]}
  (let [segment (segment-picker (range first-segment (inc last-segment)))]
    (take 2 (place/reckon-from-anchor getopt alias
              {:side side, :segment segment, :bottom bottom}))))

(defn- dig-to-seq [node]
  (if (map? node) (dig-to-seq (:hull-around node)) node))

(defn- tweak-floor-pairs
  "Produce coordinate pairs for a polygon. A reducer."
  [getopt [post-picker segment-picker bottom] coll node]
  {:post [(spec/valid? ::tarmi-core/point-coll-2d %)]}
  (let [vertex-fn (partial tweak-floor-vertex getopt segment-picker bottom)]
    (conj coll
      (if (map? node)
        ;; Pick just one post in the subordinate node, on the assumption that
        ;; they’re not all ringing the case.
        (vertex-fn (post-picker (dig-to-seq node)))
        ;; Node is one post at the top level. Always use that.
        (vertex-fn node)))))

(defn- tweak-plate-polygon
  "A single version of the footprint of a tweak.
  Tweaks so small that they amount to fewer than three vertices are ignored
  because they wouldn’t have any area."
  [getopt pickers node-list]
  (let [points (reduce (partial tweak-floor-pairs getopt pickers) [] node-list)]
    (when (> (count points) 2)
      (model/polygon points))))

(defn- tweak-plate-shadows
  "Versions of a tweak footprint.
  This is a semi-brute-force-approach to the problem that we cannot easily
  identify which vertices shape the outside of the case at z = 0."
  [getopt node-list]
  (apply maybe/union
    (distinct
      (for
        [post [first last], segment [first last], bottom [false true]]
        (tweak-plate-polygon getopt [post segment bottom] node-list)))))

(defn- all-tweak-shadows
  "The footprint of all user-requested additional shapes that go to the floor."
  [getopt]
  (apply maybe/union (map #(tweak-plate-shadows getopt (:hull-around %))
                          (filter :at-ground (main-body-tweak-data getopt)))))

(defn- case-positive-2d
  "A union of polygons representing the interior of the case, including the
  central housing, when configured to appear."
  ;; Built for maintainability rather than rendering speed.
  [getopt]
  (maybe/union
    (key/metacluster cluster-floor-polygon getopt)
    (masked-cut getopt (anchors-for-main-plate getopt))
    (all-tweak-shadows getopt)
    (when (getopt :case :central-housing :derived :include-main)
      (chousing-floor-polygon getopt))
    (when (getopt :case :central-housing :derived :include-adapter)
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
    (when (getopt :case :rear-housing :include)
      ;; To work around the problem, the rear housing floor polygon is moved a
      ;; tiny bit toward the origin, preventing the vertex overlap.
      (model/translate [0 (- wafer)] (rhousing-floor-polygon getopt)))
    (when (and (getopt :wrist-rest :include)
               (= (getopt :wrist-rest :style) :threaded))
      (model/cut (wrist/all-case-blocks getopt)))))

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
      (maybe/translate [0 0 (getopt :dfm :bottom-plate :fastener-plate-offset)]
        (holes-in-main-plate getopt)))))

;;;;;;;;;;;;;;;;;
;; Wrist Rests ;;
;;;;;;;;;;;;;;;;;

(defn wrist-anchors-positive
  "The parts of the wrist-rest plinth that receive bottom-plate fasteners."
  [getopt]
  (let [with-plate (getopt :wrist-rest :bottom-plate :include)
        thickness (if with-plate (getopt :case :bottom-plate :thickness) 0)]
    (maybe/translate [0 0 thickness]
      (anchors-in-wrist-rest getopt))))

(defn- wrist-positive-2d [getopt]
  (maybe/union
    (model/cut (wrist/unified-preview getopt))
    (model/cut (anchors-in-wrist-rest getopt))))

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
      (maybe/translate [0 0 (getopt :dfm :bottom-plate :fastener-plate-offset)]
        (holes-in-wrist-plate getopt)))))


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
      (holes-in-combo getopt))))
