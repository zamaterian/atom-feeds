(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  feeds.db
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging]))

 
(defn- clob-to-string [clob]
      "Turn a Derby 10.6.1.0 EmbedClob into a String"
      (with-open [rdr (java.io.BufferedReader. (.getCharacterStream clob))]
            (apply str (line-seq rdr))))
 
(defn find-atom-entry [feed id db] 
  (sql/with-connection db
    (sql/transaction                        
     (sql/with-query-results rs ["select id, feed, created_at, atom from atoms" ]  
        (conj  (first rs) {:atom (clob-to-string (:atom (first  rs)))} )))))

(defmacro transform [entry func]
  (if (nil? func) entry `(~func ~entry)))  

(def sql  
  "SELECT id, created_at, rank, atom, feed FROM (
         SELECT t.*, Row_Number() OVER (ORDER BY created_At) rank FROM atoms t  where feed = ?1) WHERE rank BETWEEN ?2 AND ?3 order by rank desc")

(def sql-find-archive  
  "SELECT count(*) as count FROM (
         SELECT t.*, Row_Number() OVER (ORDER BY created_At) rank FROM atoms t  where feed = ?1) WHERE rank BETWEEN ?2 AND ?3 ")
                               
(defn find-atom-feed [feed rank-start rank-end merge-into-entry db]
  (logging/debug (str rank-start " - " rank-end))
    (sql/with-connection db
      (sql/transaction
        (sql/with-query-results rs [sql feed rank-start rank-end]
          (doall  (map  (fn [x] (transform (load-string (str "'" (clob-to-string (:atom x)))) merge-into-entry )) (vec rs))))))) 
  
  
(defn- find-uuid [data] 
   (first (:content 
             (first (filter
                       (fn [x] (= :id (:tag x))) 
                       (:content data))))))

(defn insert-atom-entry
     "Insert data into the table"
   ([feed  atom_ db]
   (sql/with-connection db
      (sql/insert-values
          :atoms
          [:id :feed :atom]
          [(find-uuid atom_) feed (str atom_)])))
  ([feed atom_ date db]
   (sql/with-connection db
      (sql/insert-values
          :atoms
          [:id :feed :atom :created_at]
          [(find-uuid atom_) feed (str atom_) date]))))

(defn archive-count [feed db] 
    (sql/with-connection db
           (sql/with-query-results rs 
                ["select count(*) as count from atoms where feed =  ?" feed] 
                   (:count  (first (vec rs))))))

(defn archive-exists [feed rank-start rank-end db]
  (logging/debug (str rank-start rank-end))
  (if (and (< 0 rank-start ) (< 0 rank-end)) 
     (< 0  (sql/with-connection db
              (sql/with-query-results rs 
                [sql-find-archive feed rank-start rank-end] 
                    (:count (first (vec rs) )))))))
