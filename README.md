# Atom Feeds

## Init 
    test/derby.clj ; creates a derby test database
    test/unit/db.clj ; lazytest that calls derby.clj

## config 
  (binding [title "df" 
            uuid "df" 
            mediatype "df" 
            feed-author "A" 
            db "df" 
            url "df" 
            current-url "df"] 
    (check-config))

