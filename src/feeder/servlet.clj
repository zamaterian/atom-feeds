(ns ^{:doc "Generating servlet for compujure" :author "Thomas Engelschmidt"} feeder.servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:require [compojure.route :as route])
  (:use ring.util.servlet [feeder.routes :only [app]]))

(defservice app)
