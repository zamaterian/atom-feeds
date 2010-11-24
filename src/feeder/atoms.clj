(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeder.atoms 
  (:use [ring.commonrest :only [chk is-empty?]])) 
