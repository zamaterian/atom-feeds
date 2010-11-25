(ns ^{:doc "Generating servlet for compujure" :author "Thomas Engelschmidt"} feeds.servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:require [compojure.route :as route])
  (:use ring.util.servlet [feeds.routes :only [app]]))

(defservice app)
