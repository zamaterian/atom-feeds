(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeds.atoms 
  (:use [ring.commonrest :only [chk is-empty?]])) 

 (defn feed "" [feed ^Integer day ^Integer month ^Integer year]
   {:pre [(chk 400 (and (> day 0) (< day 32)))
          (chk 400 (and (> month 0) (< month 13)))
          (chk 400 (> year 2009))
          (chk 400 (is-empty? feed))]
    :post [(chk 404 (is-empty? %))]}
   nil ) 

   (defn entry "Find an atom entry under feed with id" [feed id]
     {:pre [(chk 400 (is-empty? feed))
          (chk 400 (is-empty? id))]
    :post [(chk 404 (is-empty? %))]} 
     nil
   )
