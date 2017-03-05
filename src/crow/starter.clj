;;; A library for starting a crow service.
;;; You can specify classpathes containing service classes in config file,
;;; so you can put a jar file containing sources of your service onto
;;; remote server, and then start the service using UrlClassLoader with
;;; this library.
(ns crow.starter
  (:require [crow.protocol :refer [install-default-marshaller]]
            [crow.configuration :as config]
            [async-connect.server :refer [close-wait]]))

(defn- load-starter-fn
  [conf]
  (if-let [starter-fn-name (:service/starter conf)]
    (let [fn-symbol (if (symbol? starter-fn-name) starter-fn-name (symbol starter-fn-name))
          fn-ns     (namespace fn-symbol)]
      (require (symbol fn-ns))
      (println "load starter-fn:" starter-fn-name)
      (if-let [starter-fn (find-var fn-symbol)]
        starter-fn
        (throw (IllegalStateException. (str "No such function: " starter-fn-name)))))
    (throw (IllegalStateException. ":service/starter key is not found in your configuration file."))))

(defn launch
  [config-path]
  (let [conf       (config/from config-path)
        starter-fn (load-starter-fn conf)]
    (println (str "SERVICE STARTS: " (:service/name conf)))
    (let [server (starter-fn conf)]
      (close-wait server #(println "SERVER STOPPED.")))))

