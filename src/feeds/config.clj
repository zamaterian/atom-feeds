(ns ^{:doc "" :author "Thomas Engelschmidt"} feeds.config  
  (:require [clojure-config.core :as config] ))

 (config/set-profiles [
             {:name "dev" :type "host" :value "darton"}
             {:name "m29841" :type "user" :value "m29841" :parent "dev"}])

(defn feed-db "Loads the property feed-db from a property-file" []
    (load-string (config/get-property "feed-db")))

(defn property [id]
    (load-string (config/get-property id)))
  
