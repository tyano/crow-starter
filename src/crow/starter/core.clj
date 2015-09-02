(ns crow.starter.core
  (:require [clojure.java.io :refer [file]]
            [clojure.edn :as edn])
  (:import [java.net URL URLClassLoader]
           [clojure.lang Var])
  (:gen-class))


(defn- resolve-classpath
  [classpath]
  (println (str "classpath: " (pr-str classpath)))
  (cond
    (symbol? classpath)
    (into-array URL [(URL. (str classpath))])

    (string? classpath)
    (into-array URL [(URL. classpath)])

    :else
    (into-array URL (map #(URL. (str %)) classpath))))

(defn- launch
  [config-path]
  (eval
    `(do
        (require 'crow.starter)
        (crow.starter/launch ~config-path))))

(defn -main
  [classpath-edn config-path & others]
  (when-not (seq config-path)
    (throw (IllegalArgumentException. "Config-path must be supplied.")))
  (when-not (.exists (file config-path))
    (throw (IllegalArgumentException. (str "the file '" config-path "' doesn't exist."))))
  (if-let [classpath (resolve-classpath (edn/read-string classpath-edn))]
    (let [loader (URLClassLoader. classpath)]
      (Var/pushThreadBindings {clojure.lang.Compiler/LOADER loader})
      (.setContextClassLoader (Thread/currentThread) loader)
      (try
        (launch config-path)
        (finally
          (Var/popThreadBindings))))
    (launch config-path)))
