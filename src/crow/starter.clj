;;; A library for starting a crow service.
;;; You can specify classpathes containing service classes in config file,
;;; so you can put a jar file containing sources of your service onto
;;; remote server, and then start the service using UrlClassLoader with
;;; this library.
(ns crow.starter
  (:require [crow.protocol :refer [install-default-marshaller]]
            [crow.configuration :as config]))

(defn- load-starter-fn
  [conf]
  (if-let [starter-fn-name (:starter conf)]
    (let [fn-symbol (if (symbol? starter-fn-name) starter-fn-name (symbol starter-fn-name))
          fn-ns     (namespace fn-symbol)]
      (require (symbol fn-ns))
      (println "load starter-fn:" starter-fn-name)
      (if-let [starter-fn (find-var fn-symbol)]
        starter-fn
        (throw (IllegalStateException. (str "No such function: " starter-fn-name)))))
    (throw (IllegalStateException. ":starter key is not found in your configuration file."))))

(defn launch
  [config-path]
  (let [conf       (if (.endsWith config-path ".clj")
                      (config/from-path config-path)
                      (config/from-edn config-path))
        marshaller (or (:object-marshaller conf)
                       (throw (IllegalStateException.
                                "Couldn't get an instance of object-marshaller. Mayby no :object-marshaller in config file.")))]
    (install-default-marshaller marshaller)
    (let [starter-fn (load-starter-fn conf)]
      (println (str "SERVICE STARTS: " (:name conf)))
      (let [server (starter-fn conf)]
        (.. (Runtime/getRuntime) (addShutdownHook (Thread. (fn [] (.close server) (println "SERVER STOPPED.")))))
        (while true
          (Thread/sleep 1000))))))

