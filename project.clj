(defproject dactyl-keyboard "0.7.0-SNAPSHOT"
  :description "A parametrized keyboard design tool"
  :url "https://viktor.eikman.se/article/the-dmote/"
  :license {:name "GNU Affero General Public License"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.3.7"]
                 [org.flatland/ordered "1.5.7"]
                 [hawk "0.2.11"]
                 ;; geom uses hiccup v1 whereas dmote-keycap uses v2.
                 [thi.ng/geom "1.0.0-RC3" :exclusions [hiccup]]
                 [clj-yaml "0.4.0"]
                 [scad-app "1.0.0"]
                 [scad-clj "0.5.3"]
                 [scad-klupe "0.2.0"]
                 [scad-tarmi "0.6.0"]
                 [dmote-keycap "0.4.0"]]
  :main dactyl-keyboard.core
  :aot :all
  :uberjar-name "dmote.jar")
