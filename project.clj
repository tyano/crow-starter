(defproject crow-starter "1.0-SNAPSHOT"
  :description "a utility library for starting crow-server."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :profiles {:provided {:dependencies [[crow "1.0-SNAPSHOT" :scope "provided"]
                                       [slingshot "0.12.2" :scope "provided"]]}}
  :repositories {"javelindev-snapshots" "http://javelindev.jp/repository/snapshots"}
  :omit-source true
  :aot [crow.starter]
  :main crow.starter)

(cemerick.pomegranate.aether/register-wagon-factory!
   "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
                      (clojure.lang.Reflector/invokeConstructor c (into-array []))))
