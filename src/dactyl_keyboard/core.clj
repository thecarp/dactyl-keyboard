;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Main Module — CLI, Final Composition and Outputs                    ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clj-yaml.core :as yaml]
            [scad-clj.model :as model]
            [scad-app.core :refer [filter-by-name
                                   refine-asset refine-all build-all]]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.dfm :refer [error-fn]]
            [dactyl-keyboard.misc :refer [output-directory soft-merge]]
            [dactyl-keyboard.sandbox :as sandbox]
            [dactyl-keyboard.param.access :as access]
            [dactyl-keyboard.param.proc.doc :refer [print-markdown-section]]
            [dactyl-keyboard.param.proc.anch :as anch]
            [dactyl-keyboard.cad.auxf :as auxf]
            [dactyl-keyboard.cad.body :as body]
            [dactyl-keyboard.cad.bottom :as bottom]
            [dactyl-keyboard.cad.central :as central]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.cad.mcu :as mcu]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.tweak :as tweak]
            [dactyl-keyboard.cad.wrist :as wrist])
  (:gen-class :main true))

(defn pprint-settings
  "Show settings as assembled from (possibly multiple) files."
  [header settings]
  (println header)
  (if (fn? settings)
    (pprint (settings))  ; Option accessor.
    (pprint settings))   ; Raw data.
  (println))

(defn document-settings
  "Show documentation for settings."
  [{section :describe-parameters}]
  (println "<!--This document was generated and is intended for rendering"
           "to HTML on GitHub. Edit the source files, not this file.-->")
  (println)
  (print-markdown-section
    (case section
      :central dactyl-keyboard.param.tree.central/raws
      :clusters dactyl-keyboard.param.tree.cluster/raws
      :main dactyl-keyboard.param.tree.main/raws
      :nested dactyl-keyboard.param.tree.nested/raws
      :ports dactyl-keyboard.param.tree.port/raws
      :wrist-rest-mounts dactyl-keyboard.param.tree.restmnt/raws
      (do (println "ERROR: Unknown section of parameters.")
          (System/exit 1))))
  (println)
  (println "⸻")
  (println)
  (println "This document was generated from the application CLI."))

(defn build-plinth-right
  "Right-hand-side non-preview wrist-rest plinth model."
  [getopt]
  (maybe/difference
    (maybe/union
      (body/mask getopt (getopt :wrist-rest :bottom-plate :include)
        (wrist/plinth-plastic getopt))
      (bottom/wrist-anchors-positive getopt)
      (when (and (getopt :wrist-rest :preview)
                 (getopt :wrist-rest :bottom-plate :include))
        (bottom/wrist-positive getopt)))
    (when (getopt :wrist-rest :bottom-plate :include)
      (bottom/holes-in-wrist-plate getopt))))

(defn- masked-inner-positive
  "Parts of the keyboard that are subject to a mask and all negatives."
  [getopt]
  (body/mask getopt (getopt :main-body :bottom-plate :include)
    (key/metacluster key/cluster-plates getopt)
    (key/metacluster body/cluster-web getopt)
    (key/metacluster body/cluster-wall getopt)
    (when (and (getopt :mcu :derived :include-laterally)
               (getopt :mcu :support :shelf :include))
      (mcu/shelf-model getopt))
    (when (and (getopt :wrist-rest :include)
               (= (getopt :wrist-rest :style) :threaded))
      (wrist/all-case-blocks getopt))
    (auxf/ports-positive getopt)
    (when (getopt :main-body :back-plate :include)
      (auxf/backplate-block getopt))
    (when (getopt :main-body :rear-housing :include)
      (body/rear-housing getopt))
    (tweak/all-main-body getopt)
    (when (getopt :main-body :bottom-plate :include)
      (bottom/anchors-in-main-body getopt))
    (auxf/foot-plates getopt)
    (when (getopt :central-housing :derived :include-adapter)
      (central/adapter-shell getopt))))

(defn- midlevel-positive
  "Parts of the keyboard that go outside the mask but should still be subject
  to all negatives."
  [getopt]
  (maybe/union
    (masked-inner-positive getopt)
    (when (and (getopt :wrist-rest :include)
               (getopt :wrist-rest :preview))
      (body/mask getopt (getopt :wrist-rest :include)
        (wrist/unified-preview getopt)))
    (when (and (getopt :wrist-rest :include)
               (not (getopt :wrist-rest :preview))
               (= (getopt :wrist-rest :style) :solid))
      (body/mask getopt (getopt :wrist-rest :include)
        (build-plinth-right getopt)))
    (when (and (getopt :main-body :bottom-plate :include)
               (getopt :main-body :bottom-plate :preview))
      (if (and (getopt :wrist-rest :include)
               (getopt :wrist-rest :bottom-plate :include)
               (getopt :main-body :bottom-plate :combine))
        (bottom/combined-positive getopt)
        (maybe/union
          (bottom/case-positive getopt)
          (when (and (getopt :wrist-rest :include)
                     (getopt :wrist-rest :preview)
                     (getopt :wrist-rest :bottom-plate :include))
            (bottom/wrist-positive getopt)))))
    (sandbox/positive getopt)))

(defn build-central-housing
  "The body of the central housing. Not subject to reflection, but generally
  bilaterally symmetrical. Chiral modules are an exception to this symmetry:
  Their positions are mirrored on the left-hand side, but their individual
  models are mirrored again to “undo” the local effects at each new position."
  [getopt]
  (let [bilateral
        (fn ([subject]  ; Non-chiral.
             (maybe/union subject (maybe/mirror [-1 0 0] subject)))
            ([subject-right subject-left]  ; Chiral.
             (maybe/union subject-right (maybe/mirror [-1 0 0] subject-left))))]
    (maybe/union
      (maybe/difference
        (body/mask getopt (getopt :main-body :bottom-plate :include)
          (maybe/difference
            (maybe/union
              (central/main-shell getopt)
              (when (getopt :main-body :bottom-plate :include)
                (bilateral (bottom/anchors-in-central-housing getopt)))
              (when (getopt :central-housing :derived :include-lip)
                (bilateral (central/lip-body-right getopt)))
              (when (getopt :central-housing :derived :include-adapter)
                (bilateral (central/adapter-fastener-receivers getopt)))
              (tweak/all-central-housing getopt))
            (when (getopt :central-housing :derived :include-adapter)
              (bilateral
                (central/adapter-right-fasteners getopt)
                (central/adapter-left-fasteners getopt)))
            (when (getopt :mcu :derived :include-centrally)
              (mcu/negative-composite getopt))
            (when (getopt :main-body :bottom-plate :include)
              (bilateral
                (bottom/holes-in-main-plate getopt)
                (bottom/holes-in-left-housing getopt))))
          (when (and (getopt :mcu :derived :include-centrally)
                     (getopt :mcu :support :shelf :include))
            (mcu/shelf-model getopt))
          (when (and (getopt :mcu :derived :include-centrally)
                     (getopt :mcu :support :lock :include))
            (mcu/lock-fixture-composite getopt))
          (sandbox/positive getopt))
        (sandbox/negative getopt))
      (when (and (getopt :main-body :bottom-plate :include)
                 (getopt :main-body :bottom-plate :preview))
        (if (and (getopt :wrist-rest :include)
                 (getopt :wrist-rest :preview)
                 (getopt :wrist-rest :bottom-plate :include))
          (bottom/combined-complete getopt)
          (bottom/case-complete getopt)))
      (when (and (getopt :mcu :derived :include-centrally)
                 (getopt :mcu :preview))
        (mcu/preview-composite getopt)))))

(defn build-main-body-right
  "Right-hand-side keyboard model."
  [getopt]
  (maybe/union
    (maybe/difference
      (maybe/union
        (maybe/difference
          (midlevel-positive getopt)
          ;; First-level negatives:
          (key/metacluster key/cluster-cutouts getopt)
          (key/metacluster key/cluster-channels getopt)
          (when (getopt :central-housing :derived :include-lip)
            ;; Space for an adapter lip, in case the adapter itself is too
            ;; thin.
            (central/lip-body-right getopt))
          (auxf/ports-negative getopt)
          (when (getopt :mcu :derived :include-laterally)
            (mcu/negative-composite getopt))
          (when (getopt :main-body :leds :include)
            (auxf/led-holes getopt))
          (when (getopt :main-body :back-plate :include)
            (auxf/backplate-fastener-holes getopt))
          (when (and (getopt :wrist-rest :include)
                     (= (getopt :wrist-rest :style) :threaded))
            (wrist/all-fasteners getopt))
          (sandbox/negative getopt))
        ;; Outer positives, subject only to outer negatives:
        (when (and (getopt :mcu :derived :include-laterally)
                   (getopt :mcu :support :lock :include))
          (mcu/lock-fixture-composite getopt)))
      ;; Outer negatives:
      (when (getopt :central-housing :derived :include-adapter)
        (central/negatives getopt))
      (when (getopt :main-body :bottom-plate :include)
        (bottom/holes-in-main-plate getopt))
      (when (and (getopt :wrist-rest :include)
                 (getopt :wrist-rest :preview)
                 (getopt :wrist-rest :bottom-plate :include))
        (bottom/holes-in-wrist-plate getopt)))
    ;; The remaining elements are visualizations for use in development.
    (when (getopt :keys :preview)
      (key/metacluster key/cluster-keycaps getopt))
    (when (and (getopt :mcu :derived :include-laterally)
               (getopt :mcu :preview))
      (mcu/preview-composite getopt))
    (when (and (getopt :central-housing :derived :include-main)
               (getopt :central-housing :preview))
      (build-central-housing getopt))))

(defn build-rubber-casting-mould-right
  "A thin shell that fits on top of the right-hand-side wrist-rest model.
  This is for casting silicone into, “in place”. If the wrist rest has
  180° rotational symmetry around the z axis, one mould should
  be enough for both halves’ wrist rests. To be printed upside down."
  [getopt]
  (place/wrist-undo getopt
    (model/difference
      (wrist/mould-polyhedron getopt)
      (wrist/unified-preview getopt)
      (bottom/wrist-anchors-positive getopt)
      (when (= (getopt :wrist-rest :style) :solid)
        (tweak/all-main-body getopt)))))

(defn build-rubber-pad-right
  "Right-hand-side wrist-rest pad model. Useful in visualization and
  prototyping, but you would not normally include a print of this in your
  final product, at least not in a hard plastic."
  [getopt]
  (place/wrist-undo getopt
    (maybe/difference
      (body/mask getopt (getopt :wrist-rest :bottom-plate :include)
        (wrist/rubber-insert-positive getopt))
      (bottom/wrist-anchors-positive getopt)
      (when (= (getopt :wrist-rest :style) :solid)
        (tweak/all-main-body getopt)))))

(def derivers-static
  "A vector of configuration locations and functions for expanding them."
  ;; Mind the order. One of these may depend upon earlier steps.
  [[[:dfm] (fn [getopt] {:compensator (error-fn (getopt :dfm :error-general))})]
   [[:keys] key/derive-style-properties]
   [[:key-clusters] key/derive-cluster-properties]
   [[:central-housing] central/derive-properties]
   [[] (fn [getopt] {:anchors (anch/collect getopt)})]
   [[:main-body :rear-housing] body/rhousing-properties]
   [[:mcu] mcu/derive-properties]
   [[:wrist-rest] wrist/derive-properties]])

(defn derivers-dynamic
  "Additions for more varied parts of a configuration."
  [getopt]
  (for [i (range (count (getopt :wrist-rest :mounts)))]
       [[:wrist-rest :mounts i] #(wrist/derive-mount-properties % i)]))

(defn enrich-option-metadata
  "Derive certain properties that are implicit in the user configuration.
  Use a gradually expanding but temporary build option accessor.
  Store the results under the “:derived” key in each section."
  [build-options]
  (reduce
    (fn [coll [path callable]]
      (soft-merge
        coll
        (assoc-in coll (conj path :derived)
                       (callable (access/option-accessor coll)))))
    build-options
    (concat derivers-static
            (derivers-dynamic (access/option-accessor build-options)))))

(defn- from-file
  "Parse raw settings out of a YAML file."
  [filepath]
  (try
    (yaml/parse-string (slurp filepath))
    (catch java.io.FileNotFoundException _
      (println (format "Failed to load file “%s”." filepath))
      (System/exit 1))))

(defn- merge-opt-file
  "Merge a single configuration file into a configuration."
  [raws filepath]
  (try
    (soft-merge raws (from-file filepath))
    (catch Exception e
      ;; Most likely a java.lang.ClassCastException or
      ;; java.lang.IllegalArgumentException from a structural problem.
      ;; When such problems do not affect a merge, they are caught on
      ;; parsing or access, i.e. later.
      (println (format "Error while merging options in file “%s”." filepath))
      (throw e))))

(defn- merge-raw-opts
  "Merge all configuration files."
  [filepaths]
  (try
    (reduce merge-opt-file {} filepaths)
    (catch Exception e
      (println
        (format (str "There may be a structural problem in any of "
                     "%s, such as a dictionary (map) in place of a list, "
                     "or vice versa.")
                filepaths))
      (throw e))))

(defn- get-accessor
  "Parse model parameters. Return an accessor for them."
  [{:keys [configuration-file debug]}]
  (let [checkpoint (fn [a b] (when debug (pprint-settings a b)) b)]
    (->>
      (merge-raw-opts configuration-file)
      (checkpoint "Received settings without built-in defaults:")
      (access/checked-configuration)
      (checkpoint "Resolved and validated settings:")
      enrich-option-metadata
      access/option-accessor
      (checkpoint "Enriched settings:"))))

(def module-asset-list
  "OpenSCAD modules and the functions that make them."
  [{:name "housing_adapter_fastener"
    :model-precursor central/build-fastener,
    :chiral true}
   {:name "sprue_negative"
    :model-precursor wrist/sprue-negative}
   {:name "bottom_plate_anchor_positive_nonprojecting"
    :model-precursor bottom/anchor-positive-nonprojecting}
   {:name "bottom_plate_anchor_positive_central"
    :model-precursor bottom/anchor-positive-central}
   {:name "bottom_plate_anchor_negative"
    :model-precursor bottom/anchor-negative,
    :chiral true}])

(defn module-asset-map
  "Convert module-asset-list to a hash map with fully resolved models.
  Add a variable number of additional modules based on key styles."
  [getopt]
  (merge
    (reduce  ; Static.
      (fn [coll {:keys [name model-precursor] :as asset}]
        (assoc coll name
          (assoc asset :model-main (model-precursor getopt))))
      {}
      module-asset-list)
    (reduce  ; Dynamic.
      (fn [coll key-style]
        (let [prop (getopt :keys :derived key-style)
              {:keys [switch-type module-keycap module-switch]} prop]
          (assoc coll
            module-keycap
            {:name module-keycap
             :model-main (key/single-cap getopt key-style false)}
            module-switch  ; Uniqueness of input not guaranteed.
            {:name module-switch
             :model-main (key/single-switch getopt switch-type)})))
      {}
      (keys (getopt :keys :styles)))))

(defn- get-key-modules
  "Produce a sorted vector of module name strings for user-defined key styles."
  [getopt & property-keys]
  (sort
    (into []
      (reduce
        (fn [coll data] (apply (partial conj coll) (map data property-keys)))
        #{}
        (vals (getopt :keys :derived))))))

(defn- conditional-bottom-plate-modules
  [getopt]
  (if (getopt :main-body :bottom-plate :include)
    ["bottom_plate_anchor_positive_nonprojecting",
     "bottom_plate_anchor_positive_central",
     "bottom_plate_anchor_negative"]
    []))

(defn- central-housing-modules
  "A collection of OpenSCAD modules for the central housing."
  [getopt]
  (concat
    [(when (getopt :central-housing :derived :include-adapter)
       "housing_adapter_fastener")]
    (conditional-bottom-plate-modules getopt)))

(defn get-static-precursors
  "Make the central roster of files and the models that go into each.
  The schema used to describe them is a superset of the scad-app
  asset schema, adding dependencies on special configuration values and
  rotation for ease of printing. The models themselves are described with
  unary precursors that take a completed “getopt” function."
  [getopt]
  [{:name "preview-keycap-clusters"
    :modules (get-key-modules getopt :module-keycap)
    :model-precursor (partial key/metacluster key/cluster-keycaps)}
   {:name "case-main"
    :modules (concat
               [(when (getopt :central-housing :derived :include-adapter)
                  "housing_adapter_fastener")
                (when (getopt :wrist-rest :sprues :include)
                  "sprue_negative")]
               (conditional-bottom-plate-modules getopt)
               (get-key-modules getopt :module-keycap :module-switch))
    :model-precursor build-main-body-right
    :chiral (getopt :main-body :reflect)}
   (when (getopt :central-housing :derived :include-main)
     {:name (str "case-central"  ; With conditional suffix.
              (when (getopt :central-housing :derived :include-sections)
                "-full"))
      :modules (central-housing-modules getopt)
      :model-precursor build-central-housing})
   (when (and (getopt :mcu :include)
              (getopt :mcu :support :lock :include))
     {:name "mcu-lock-bolt"
      :model-precursor mcu/lock-bolt-model
      :rotation [0 π 0]})
   ;; Wrist rest:
   (when (getopt :wrist-rest :include)
     {:name "pad-mould"
      :modules (conditional-bottom-plate-modules getopt)
      :model-precursor build-rubber-casting-mould-right
      :rotation [π 0 0]
      :chiral (getopt :main-body :reflect)})  ; Chirality is possible but not guaranteed.
   (when (getopt :wrist-rest :include)
     {:name "pad-shape"
      :modules (conditional-bottom-plate-modules getopt)
      :model-precursor build-rubber-pad-right
      :chiral (getopt :main-body :reflect)})
   (when (and (getopt :wrist-rest :include)
              (not (= (getopt :wrist-rest :style) :solid)))
     {:name "wrist-rest-main"
      :modules (concat (conditional-bottom-plate-modules getopt)
                       (when (getopt :wrist-rest :sprues :include)
                         ["sprue_negative"]))
      :model-precursor build-plinth-right
      :chiral (getopt :main-body :reflect)})
   ;; Bottom plate(s):
   (when (and (getopt :main-body :bottom-plate :include)
              (not (and (getopt :main-body :bottom-plate :combine)
                        (getopt :wrist-rest :bottom-plate :include))))
     {:name "bottom-plate-case"
      :modules (conditional-bottom-plate-modules getopt)
      :model-precursor bottom/case-complete
      :rotation [0 π 0]
      :chiral (getopt :main-body :reflect)})
   (when (and (getopt :wrist-rest :include)
              (getopt :wrist-rest :bottom-plate :include)
              (not (and (getopt :main-body :bottom-plate :include)
                        (getopt :main-body :bottom-plate :combine))))
     {:name "bottom-plate-wrist-rest"
      :modules (conditional-bottom-plate-modules getopt)
      :model-precursor bottom/wrist-complete
      :rotation [0 π 0]
      :chiral (getopt :main-body :reflect)})
   (when (and (getopt :main-body :bottom-plate :include)
              (getopt :main-body :bottom-plate :combine)
              (getopt :wrist-rest :include)
              (getopt :wrist-rest :bottom-plate :include))
     {:name "bottom-plate-combined"
      :modules (conditional-bottom-plate-modules getopt)
      :model-precursor bottom/combined-complete
      :rotation [0 π 0]
      :chiral (getopt :main-body :reflect)})])

(defn get-key-style-precursors
  "Collate key-style precursors. No maquettes though; they’re no fun to print."
  [getopt]
  (mapcat
    (fn [key-style]
      (if-not (= (getopt :keys :derived key-style :style) :maquette)
        [{:name (str "keycap-" (name key-style))
          :model-precursor #(key/single-cap % key-style true)}]))
    (keys (getopt :keys :styles))))

(defn get-dfm-subassemblies
  "Collate model precursors for subassemblies.
  This currently consists of central housing sections only."
  [getopt]
  (when (getopt :central-housing :derived :include-sections)
    (map-indexed
      (fn [idx [left right]]
        {:name (str "case-central-section-" (inc idx))
         :modules (central-housing-modules getopt)
         :model-precursor
           (fn [getopt]
             (model/rotate [0 (/ π (if (zero? idx) 2 -2)) 0]
               (model/intersection
                 (model/translate [(+ left (/ (- right left) 2)) 0 0]
                   (model/cube (- right left) 1000 1000))
                 (build-central-housing getopt))))})
      (->>
        (getopt :dfm :central-housing :sections)
        (concat [-1000 1000])  ; Add left- and right-hand-side bookends.
        (sort)
        (partition 2 1)))))

(defn get-all-precursors
  "Add dynamic elements to static precursors."
  [getopt]
  (concat
    (get-static-precursors getopt)
    (get-key-style-precursors getopt)
    (get-dfm-subassemblies getopt)))

(defn- finalize-asset
  "Define scad-app asset(s) from a single proto-asset.
  Return a vector of one or two assets."
  [getopt module-map cli-options
   {:keys [model-precursor rotation modules]
    :or {rotation [0 0 0], modules []}
    :as proto-asset}]
  (refine-asset
    {:original-fn #(str "right-hand-" %),
     :mirrored-fn #(str "left-hand-" %)}
    (conj
      (select-keys proto-asset [:name :chiral])  ; Simplified base.
      [:model-main (maybe/rotate rotation (model-precursor getopt))]
      (when (getopt :resolution :include)
        [:minimum-face-size (getopt :resolution :minimum-face-size)]))
    (map (partial get module-map) (remove nil? modules))))

(defn- finalize-all
  [cli-options]
  (let [getopt (get-accessor cli-options)
        module-map (module-asset-map getopt)
        requested (remove nil? (get-all-precursors getopt))]
    (refine-all requested
      {:refine-fn (partial finalize-asset getopt module-map)})))

(defn- output-filepath-fn
  [base suffix]
  "Produce a relative file path for e.g. SCAD or STL."
  (io/file output-directory suffix (str base "." suffix)))

(defn run
  "Build all models, authoring files in parallel. Easily used from a REPL."
  [{:keys [whitelist render renderer] :or {whitelist #""} :as options}]
  (build-all (filter-by-name whitelist (finalize-all options))
             {:render render
              :rendering-program renderer
              :filepath-fn output-filepath-fn}))

(defn execute-mode
  "Act on arguments received from the command line (shell), already parsed.
  If arguments are erroneous, show how, else react to flags for special modes,
  else proceed to the default mode, which is building models.
  Return an appropriate Unix exit code."
  [{:keys [errors summary options]}]
  (let [{:keys [help describe-parameters debug]} options]
    (cond
      (some? errors) (do (println (first errors)) (println summary))
      help (println summary)
      describe-parameters (document-settings options)
      :else (do (run options) (when debug (println "Exiting without error."))))
    (if (some? errors) 1 0)))

(def cli-options
  "Define command-line interface for using the application from the shell."
  [["-c" "--configuration-file PATH" "Path to parameter file in YAML format"
    :default []
    :assoc-fn (fn [m k new] (update-in m [k] (fn [old] (conj old new))))]
   [nil "--describe-parameters SECTION"
    "Print a Markdown document specifying what a configuration file may contain"
    :default nil :parse-fn keyword]
   [nil "--render" "Produce STL in addition to SCAD files"]
   [nil "--renderer PATH" "Path to OpenSCAD" :default "openscad"]
   ["-w" "--whitelist RE"
    "Limit output to files whose names match the regular expression RE"
    :parse-fn re-pattern]
   ["-d" "--debug"]
   ["-h" "--help"]])

(defn -main
  "Parse command-line arguments, act on them and exit the application."
  [& raw]
  (try
    (System/exit (execute-mode (parse-opts raw cli-options)))
    (catch clojure.lang.ExceptionInfo e
      ;; Likely raised by getopt.
      (println "An exception occurred:" (.getMessage e))
      (pprint (ex-data e))
      (System/exit 1))))
