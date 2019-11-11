;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Documentation Formatting                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This file extracts built-in documentation from application parameters.

(ns dactyl-keyboard.param.doc
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as spec]
            [dactyl-keyboard.param.base :as base]
            [dactyl-keyboard.param.schema :as schema]))


;;;;;;;;;;;;;;
;; Internal ;;
;;;;;;;;;;;;;;

(defn- default-doc-heading
  "Produce a default heading template for documents."
  [is-param]
  (if is-param "Parameter %s" "Section %s"))

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
  (doseq [[key {:keys [path metadata] :as value}] node]
    (let [is-param (spec/valid? ::schema/parameter-spec value)]
      (when (not (= key :metadata))
        (println
          (str (string/join "" (repeat level "    ")) "-")
          (format (get value :heading-template (default-doc-heading is-param))
                  (link-to-anchor (if is-param path (:path metadata))))))
      (if (not is-param)
        (print-markdown-toc value (inc level))))))

(defn- print-markdown-fragment
  "Print a description of a node in the settings structure using Markdown."
  [node level]
  (let [heading-style
          (fn [text]
            (if (< level 7)  ; Markdown only supports up to h6 tags.
              (str (string/join "" (repeat level "#")) " " text)
              (str "###### " text " at level " level)))]
    (doseq [[key {:keys [path help metadata] :as value}] node]
      (let [is-param (spec/valid? ::schema/parameter-spec value)]
        (when (not (= key :metadata))
          (println)
          (println
            (heading-style
              (format
                (get value :heading-template (default-doc-heading is-param))
                (target-anchor (if is-param path (:path metadata))))))
          (println)
          (println (or (if is-param help (:help metadata)) "Undocumented.")))
        (if (not is-param)
          (print-markdown-fragment value (inc level)))))))


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
