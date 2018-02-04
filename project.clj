(defproject crow-starter "2.0-SNAPSHOT"
  :description "a utility library for starting crow-server."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:provided {:dependencies [[crow "2.0-SNAPSHOT" :scope "provided"]]}}
  :repositories {"javelindev-snapshots" "http://javelindev.jp/repository/snapshots"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :aot [crow.starter.core]
  :main crow.starter.core)
