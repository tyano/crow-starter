;;; A library for starting a crow service.
;;; You can specify classpathes containing service classes in config file,
;;; so you can put a jar file containing sources of your service onto
;;; remote server, and then start the service using UrlClassLoader with
;;; this library.
(ns crow.starter
  (:require [crow.registrar :refer [start-registrar-service]]
            [crow.service :refer [start-service]]
            [crow.protocol :refer [install-default-marshaller]]
            [clojure.string :refer [split]]
            [crow.configuration :as config]
            [clojure.tools.logging :as log]
            [crow.logging :refer [trace-pr]]))

(defmulti start
  "start a service by the :type of service (:registrar or :service)."
  (fn [config] (:type config)))

(defmethod start :registrar
  [config]
  (start-registrar-service config))

(defmethod start :service
  [config]
  (start-service config))

(defmulti initialize
  "initialize a service before starting the service."
  (fn [config] (:name config)))

(defmethod initialize :default
  [config]
  ;;do nothing
  )


(defn- instanciate
  [class-name]
  {:pre [(seq class-name)]}
  (let [loader (.getContextClassLoader (Thread/currentThread))
        clazz  (Class/forName class-name true loader)]
    (when clazz
      (.newInstance clazz))))

(defn- load-marshaller
  [conf]
  (when-let [marshaller-def (:object-marshaller conf)]
    (if (string? marshaller-def) (instanciate marshaller-def) marshaller-def)))

(defn- load-initializer
  [conf]
  (when-let [initializer (:initializer conf)]
    (println "load initializer.")
    (require initializer)))

(defn- do-start
  [conf]
  (let [marshaller (or (load-marshaller conf)
                       (throw (IllegalStateException.
                                "Couldn't get an instance of object-marshaller. Mayby no :object-marshaller in config file.")))]
    (install-default-marshaller marshaller)
    (load-initializer conf)
    (initialize conf)
    (println (str "SERVICE STARTS: " (:name conf)))
    (start conf)))

(defn launch
  [config-path]
  (let [conf (config/from-path config-path)]
    (do-start conf)))

