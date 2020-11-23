;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Documentation Formatting                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This file extracts built-in documentation from application parameters.

(ns dactyl-keyboard.param.proc.doc
  (:require [clojure.string :as string]
            [dactyl-keyboard.param.base :as base]))


;;;;;;;;;;;;;;
;; Internal ;;
;;;;;;;;;;;;;;

(defn- heading-style
  [level text]
  (if (< level 7)  ; Markdown only supports up to h6 tags.
    (str (string/join "" (repeat level "#")) " " text)
    (str "###### " text " at level " level)))

(defn- compose-heading
  "Produce and fill a heading template for documents."
  [{:keys [heading-template freely-keyed leaf] :or {freely-keyed false}} content]
  {:post (string? %)}
  (format
    (cond
      heading-template heading-template
      freely-keyed "Freely keyed section %s"
      leaf "Parameter %s"
      :else "Section %s")
    content))

(defn- slugify
  "Represent a section or parameter as a unique string for HTML anchors.
  Take an iterable of keywords."
  [path]
  (string/join "-" (map name path)))

(defn- target-anchor
  "Tag a setting name as HTML navigable by its path of keywords.
  Notice that on GitHub as of 2019, the ID string provided here will not be
  used as is, but will enstead be prefixed by “user-content-” for namespacing."
  [path]
  (format "<a id=\"%s\">`%s`</a>" (slugify path) (name (last path))))

(defn- link-to-anchor
  "Produce HTML for a hyperlink to a local anchor on a GitHub page.
  Notice the injection of a “user-content-” prefix to counter the behaviour
  of GitHub’s Markdown parser as of 2019-11. If the project moves to another
  site, or GitHub repents, this will break."
  [path]
  (format "<a href=\"#user-content-%s\">`%s`</a>"
    (slugify path) (name (last path))))

(defn- print-markdown-toc
  "Print an indented list as a table of contents. Descend recursively."
  [node level]
  (doseq [[key {::base/keys [metadata] :as value}] node]
    (when-not (= key ::base/metadata)
      (let [{:keys [path]} metadata]
        (println (str (string/join "" (repeat level "    ")) "-")
                 (compose-heading metadata (link-to-anchor path)))
        (print-markdown-toc value (inc level))))))

(defn- print-markdown-fragment
  "Print a description of a node in the settings structure using Markdown."
  [node level]
  (doseq [[key {::base/keys [metadata] :as value}] node]
    (let [{:keys [help leaf path]} metadata]
      (when-not (= key ::base/metadata)
        (println)
        (println
          (heading-style level (compose-heading metadata (target-anchor path))))
        (when-not (zero? (count help)) (println) (println help))
        (when-not leaf (print-markdown-fragment value (inc level)))))))


;;;;;;;;;;;;;;;
;; Interface ;;
;;;;;;;;;;;;;;;

(defn print-markdown-section
  "Print documentation for a section based on its flat specifications.
  Use the first entry as an introduction."
  [flat]
  (println (apply str (first flat)))
  (println)
  (let [inflated (base/inflate flat)]
    (println "## Table of contents")
    (print-markdown-toc inflated 0)
    (print-markdown-fragment inflated 2)))
