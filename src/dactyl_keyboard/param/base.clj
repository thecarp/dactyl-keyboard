;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Basics                                              ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.param.base
  (:require [clojure.spec.alpha :as spec]
            [flatland.ordered.map :refer [ordered-map]]
            [dactyl-keyboard.misc :refer [soft-merge]]))


;;;;;;;;;;;;;;
;; Internal ;;
;;;;;;;;;;;;;;

(spec/def ::default any?)
(spec/def ::freely-keyed boolean?)
(spec/def ::heading-template string?)
(spec/def ::help string?)
(spec/def ::leaf boolean?)
(spec/def ::parse-fn fn?)
(spec/def ::path (spec/coll-of keyword?))
(spec/def ::resolve-fn fn?)
(spec/def ::validate (spec/coll-of some?))  ; Anything spec can check against.

(spec/def ::raw  ; Bare metadata as given in configuration specifications.
  (spec/keys :opt-un [::default ::freely-keyed ::heading-template ::parse-fn
                      ::validate]))
(spec/def ::metadata  ; Full metadata with interpolations.
  (spec/and ::raw (spec/keys :req-un [::path ::help ::leaf])))

(defn- split-tail
  "Handle an optional map of metadata followed by optional docstrings."
  [data]
  (if (-> data first string?) [{} data] [(first data) (rest data)]))

(defn- coalesce
  "Assemble one branch in a tree structure from flat specifications.
  Keep paths around for ease of building tables of content."
  [coll [path & other]]
  (let [[metadata helptext] (split-tail other)]
    (when-not (spec/valid? ::raw metadata)
      (throw (ex-info "Invalid configuration metadata"
                      {:type :internal-error
                       :path path
                       :raw-metadata metadata
                       :spec-explanation (spec/explain-str ::raw metadata)})))
    (->> metadata
      (merge {:leaf (contains? metadata :default)
              :path path
              :help (apply str helptext)})
      (assoc (ordered-map) ::metadata)
      (assoc-in coll path))))

(defn inclusive-or
  "A merge strategy for configuration keys. Take everything.
  Exposed for unit testing."
  [nominal candidate]
  (apply concat (map keys [nominal candidate])))

(defn- explicit-only
  "A merge strategy for configuration keys. Take only what the user provides."
  [_ candidate]
  (keys candidate))

(defn- expand-exception
  [exception key]
  (let [data (ex-data exception)
        new-data (assoc data :keys (cons key (get data :keys ())))]
   (throw (ex-info (.getMessage exception) new-data))))

(defmacro expand-any-exception
  [key call]
  (let [sym (gensym)]
    `(try
       ~call
       (catch clojure.lang.ExceptionInfo ~sym
         (expand-exception ~sym ~key)))))

(defn- traverse-node
  "Treat a branch or leaf. Raise an exception on superfluous entries.
  Branches and leaves are distinguished by arity."
  ;; TODO: Track path for use in exceptions.
  ([leafer key-picker nominal candidate]
   (when-not (map? candidate)
     (throw (ex-info "Non-mapping section in configuration file"
                     {:type :structural-error
                      :raw-value candidate})))
   (reduce
     (fn [coll key]
       (let [metadata (get-in nominal [key ::metadata])]
         (when-not (spec/valid? ::metadata metadata)
           (throw (ex-info "Superfluous entry in configuration file"
                           {:type :superfluous-key
                            :keys (list key)
                            :accepted-keys (keys nominal)})))
         (assoc coll key
           (if (:leaf metadata)
             ;; Entry is a leaf.
             (traverse-node leafer key-picker nominal candidate key)
             ;; Else entry is a branch.
             (traverse-node leafer key-picker (key nominal) (key candidate {}))))))
     candidate
     (remove #(= ::metadata %) (distinct (key-picker nominal candidate)))))
  ([leafer _ nominal candidate key]
   (when-not (contains? nominal key)
     (throw (ex-info "Superfluous configuration key"
                     {:type :superfluous-key
                      :keys (list key)
                      :accepted-keys (keys nominal)})))
   (expand-any-exception key (leafer (key nominal) (key candidate)))))


;;;;;;;;;;;;;;;
;; Interface ;;
;;;;;;;;;;;;;;;

(defn hard-defaults
  "Pick a user-supplied value over a default value.
  This is the default method for resolving overlap between built-in defaults
  and the user configuration, at the leaf level."
  [{:keys [default]} candidate]
  (or candidate default))

(defn soft-defaults
  "Prioritize a user-supplied value over a default value, but make it spongy.
  This is an alternate method for resolving overlap, intended for use with
  defaults that are so complicated the user will not want to write a complete,
  explicit replacement every time."
  [{:keys [default]} candidate]
  (soft-merge default candidate))

(defn inflate
  "Recursively assemble a tree from flat specifications.
  Skip the first entry, assuming it’s documentation."
  [flat]
  (reduce coalesce (ordered-map) (rest flat)))


;; Parsing:

(defn parse-leaf
  "Resolve differences between settings from different sources.
  Run the result through a specified parsing function and return it."
  [{::keys [metadata]} candidate]
  (let [{:keys [parse-fn resolve-fn]
         :or {parse-fn identity, resolve-fn hard-defaults}} metadata
        merged (resolve-fn metadata candidate)]
    (try
      (parse-fn merged)
      (catch Exception e
        (throw (ex-info "Could not cast value to correct data type"
                        {:type :parsing-error
                         :raw-value candidate
                         :merged-value merged
                         :parser parse-fn
                         :original-exception e}))))))

(def parse-inclusively (partial traverse-node parse-leaf inclusive-or))
(def parse-explicit (partial traverse-node parse-leaf explicit-only))

;; Validation:

(defn validate-leaf
  "Validate a specific parameter received through the UI.
  Side effects (exception) only. The return value should not be used."
  ;; Exposed for unit testing.
  [{::keys [metadata]} candidate]
  (let [{:keys [leaf validate] :or {validate []}} metadata]
    (assert leaf)
    (doseq [validator validate]
      (if-not (spec/valid? validator candidate)
        (throw (ex-info "Value out of range"
                        {:type :validation-error
                         :parsed-value candidate
                         :spec-explanation (spec/explain-str validator candidate)}))))))

(def validate-branch (partial traverse-node validate-leaf explicit-only))

(defn delegated-validation
  "Make a function to delegate the validation of a branch."
  [raws]
  (partial validate-branch (inflate raws)))


;; Both/other:

(defn consume-branch
  "Parse a branch and then validate it.
  Trust validation failure to raise an exception."
  [nominal candidate]
  (let [parsed (parse-inclusively nominal candidate)]
    (validate-branch nominal parsed)
    parsed))

(defn parser-with-defaults
  "Make a function to parse a branch. Close over its raw specifications."
  [raws]
  (partial parse-inclusively (inflate raws)))

(defn parser-wo-defaults
  "Make a function to parse a branch without its default values.
  This is useful for parts of the configuration that can be overridden."
  [raws]
  (partial parse-explicit (inflate raws)))

(defn extract-defaults
  "Fetch default values for a broad section of the configuration."
  [raws]
  ((parser-with-defaults raws) {}))
