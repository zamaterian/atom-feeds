(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  feeds.db
  (:use clojure-config.core)
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging]))

(defn- feed-db "Loads the property feed-db from a property-file" []
      (load-string (get-property "feed-db")))

 
(defn- clob-to-string [clob]
      "Turn a Derby 10.6.1.0 EmbedClob into a String"
      (with-open [rdr (java.io.BufferedReader. (.getCharacterStream clob))]
            (apply str (line-seq rdr))))
 
(defn find-atom-entry [feed id] 
  (sql/with-connection (feed-db)
    (sql/transaction                        
     (sql/with-query-results rs ["select id, feed, created_at, atom from atoms" ]  
        (conj  (first rs) {:atom (clob-to-string (:atom (first  rs)))} )))))


(defn- str-date "Handle oracle date and derby based on feed-db properties" [day month year operator] 
  (if (not (empty? (re-find #"derby" (let [db (get-property "feed-db")] (if (nil? db) "" db)))))
           (str "created_at  " operator " {d '" year "-" month "-" day "'}")
           (str "trunc (created_at) " operator " to_date ('" year  "-" month "-" day " 00:00:00','YYYY-MM-DD HH24:Mi:SS')")))

(defn find-atom-feed [feed day month year] 
  (let [date (str-date day month year "=")
        sql (str "select id, feed, created_at, atom from atoms where feed = ? and " date " order by created_at desc")]
    (sql/with-connection (feed-db)
      (sql/transaction
        (sql/with-query-results rs [sql feed ]
                    (doall  (map  (fn [x] (load-string (str "'" (clob-to-string (:atom x))))) (vec rs)))))))) 
  
  
(defn- find-uuid [data] 
   (first (:content 
             (first (filter
                       (fn [x] (= :id (:tag x))) 
                       (:content data))))))

(defn insert-atom-entry
     "Insert data into the table"
     [feed  atom_]
   (sql/with-connection (feed-db)
      (sql/insert-values
          :atoms
          [:id :feed :atom]
          [(find-uuid atom_) feed (str atom_)])))

(defn find-prev-archive-date [feed day month year] 
   (sql/with-connection (feed-db)
               (sql/with-query-results rs 
                 [ (logging/spy (str "select  distinct created_at from atoms where rownum = 1 and feed = ? and " (str-date day month year "<")" order by created_at DESC ")) feed ] 
                     (:created_at  (get (vec rs) 0)))))

(defn find-next-archive-date [feed day month year ] 
   (sql/with-connection (feed-db)
               (sql/with-query-results rs 
                    [(logging/spy (str "select  distinct created_at from atoms  where rownum = 1 and feed = ? and "(str-date day month year ">") " order by created_at asc ")) feed ] 
                        (:created_at  (get (vec rs) 0)))))
