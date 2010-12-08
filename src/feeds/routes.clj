(ns ^{:doc "Http layer for Atom Feeds" :author "Thomas Engelschmidt" } feeds.routes 
  (:use compojure.core
           ring.util.response
           ring.util.servlet
           clojure.contrib.json
           ring.commonrest
           feeds.config
           [feeds.atoms :as atoms])
         (:require [compojure.route :as route]
                   [clojure.contrib.logging :as logging]))


(defroutes handler

    (GET "/xml/" [] (atoms/build-feed))

    (GET "*/alive" [] (str "OK"))

    (GET "/:feed/feed/:day/:month/year/" [feed day month year] 
      (atoms/feed feed day month year))

    (GET "/:feed/notification/:id" [feed id] 
      (atoms/entry feed id))

    (POST "/:feed/notification/" [feed body] 
     nil); todo validate payload as valid a atom entry


    (route/not-found (route-not-found-text)))
           
(def app
    (-> (var handler)
    ; order is important first wrap-request-log-and-error-handling - then wrap-json-params; then wrap-promote-header
    (wrap-request-log-and-error-handling)
    (wrap-promote-header)))

