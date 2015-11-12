;;; A library for starting a crow service.
;;; You can specify classpathes containing service classes in config file,
;;; so you can put a jar file containing sources of your service onto
;;; remote server, and then start the service using UrlClassLoader with
;;; this library.
(ns crow.starter
  (:require [crow.protocol :refer [install-default-marshaller]]
            [clojure.string :refer [split]]
            [crow.configuration :as config]
            [clojure.tools.logging :as log]
            [crow.logging :refer [trace-pr]]))

(defn- instanciate
  [fn-name]
  {:pre [fn-name]}
  (let [fn-symbol (if (symbol? fn-name) fn-name (symbol fn-name))
        fn-ns     (namespace fn-symbol)]
    (require (symbol fn-ns))
    (if-let [marshaller-constructor (find-var fn-name)]
      (marshaller-constructor)
      (throw (IllegalStateException. (str "No such fn: " fn-name))))))

(defn- load-marshaller
  [conf]
  (when-let [marshaller-def (:object-marshaller conf)]
    (if (or (string? marshaller-def) (symbol? marshaller-def))
      (instanciate marshaller-def)
      marshaller-def)))

(defn- load-starter-fn
  [conf]
  (if-let [starter-fn-name (:starter conf)]
    (let [fn-symbol (if (symbol? starter-fn-name) starter-fn-name (symbol starter-fn-name))
          fn-ns     (namespace fn-symbol)]
      (require (symbol fn-ns))
      (println "load starter-fn:" starter-fn-name)
      (if-let [starter-fn (find-var starter-fn-name)]
        starter-fn
        (throw (IllegalStateException. (str "No such function: " starter-fn-name)))))
    (throw (IllegalStateException. ":starter key is not found in your configuration file."))))

(defn launch
  [config-path]
  (let [conf       (config/from-path config-path)
        marshaller (or (load-marshaller conf)
                       (throw (IllegalStateException.
                                "Couldn't get an instance of object-marshaller. Mayby no :object-marshaller in config file.")))]
    (install-default-marshaller marshaller)
    (let [starter-fn (load-starter-fn conf)]
      (println (str "SERVICE STARTS: " (:name conf)))
      (starter-fn conf))))

