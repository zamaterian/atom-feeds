(ns ^{:doc "Http layer for Atom Feeds" :author "Thomas Engelschmidt" } feeds.routes 
  (:use compojure.core
           ring.middleware.json-params
           ring.util.response
           ring.util.servlet
           clojure.contrib.json
           ring.commonrest
           [feeds.atoms :as atoms])
         (:require [compojure.route :as route]
                      [clojure.contrib.logging :as logging]))

(defroutes handler
    (GET "*/alive" [] (str "OK"))


    (route/not-found (route-not-found-text)))
           
(def app
    (-> (var handler)
    ; order is important first wrap-request-log-and-error-handling - then wrap-json-params; then wrap-promote-header
    (wrap-request-log-and-error-handling)
    (wrap-json-params) 
    (wrap-promote-header)))

