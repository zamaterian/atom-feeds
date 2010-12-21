(ns ^{:doc "Example Http layer for Atom Feeds" :author "Thomas Engelschmidt" } routes 
  (:use compojure.core
           ring.util.response
           ring.util.servlet
           clojure.contrib.json
           ring.commonrest
           feeds.config
           [feeds.atoms :as atoms])
         (:require [compojure.route :as route]
                   [clojure.contrib.logging :as logging]))


(defroutes atom-handler

    (GET "/:feed/notifications/:day/:month/year/" [feed day month year] 
      (atoms/find-feed feed day month year))

    (GET "/:feed/notification/:id" [feed id] 
      (atoms/find-entry feed id))

    (GET "/:feed/notifications/" [feed id] 
      (atoms/find-current feed ))

    (route/not-found (route-not-found-text)))
           
(def app
    (-> (var atom-handler)
    ; order is important first wrap-request-log-and-error-handling - then wrap-json-params; then wrap-promote-header
    (wrap-request-log-and-error-handling)
    (wrap-promote-header)))

