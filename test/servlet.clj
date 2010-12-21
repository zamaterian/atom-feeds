(ns ^{:doc "Generating servlet for compujure" :author "Thomas Engelschmidt"} servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:require [compojure.route :as route])
  (:use ring.util.servlet [routes :only [app]]))

(defservice app)
