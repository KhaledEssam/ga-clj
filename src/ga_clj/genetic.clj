(ns ga-clj.genetic
  (:require [progrock.core :as pr]))

;; Helpers: Generate Random Coordinates
;; Copied from http://ccann.github.io/2016/05/05/random-coordinates.html

(defn ->coord
  [coord-type n]
  (when n
    (case coord-type
      :lat (assert (and (>= n -90) (<= n 90)))
      :lon (assert (and (>= n -180) (<= n 180))))
    (->> n (double) (format "%.6f") (Double.))))

(defn rand-lat []
  (let [random (java.util.Random.)
        max 90
        min -90
        diff (- max min)]
    (->> random (.nextDouble) (* diff) (+ min) (->coord :lat))))

(defn rand-lng []
  (let [random (java.util.Random.)
        max 180
        min -180
        diff (- max min)]
    (->> random (.nextDouble) (* diff) (+ min) (->coord :lon))))

(defn rand-coord
  ([] {:lng (rand-lng) :lat (rand-lat)})
  ([n] (for [_ (range n)] (rand-coord))))


;; Computing Haversine Distance
;; Copied from https://gist.github.com/shayanjm/39418c8425c2a66d480f

(defn haversine
  "Implementation of Haversine formula.
   Takes two sets of latitude/longitude pairs and returns the shortest great circle distance between them (in km)."
  [{lon1 :lng lat1 :lat} {lon2 :lng lat2 :lat}]
  (let [R 6378.137 ; Radius of Earth in km
        dlat (Math/toRadians (- lat2 lat1))
        dlon (Math/toRadians (- lon2 lon1))
        lat1 (Math/toRadians lat1)
        lat2 (Math/toRadians lat2)
        a (+ (* (Math/sin (/ dlat 2)) (Math/sin (/ dlat 2))) (* (Math/sin (/ dlon 2)) (Math/sin (/ dlon 2)) (Math/cos lat1) (Math/cos lat2)))]
    (* R 2 (Math/asin (Math/sqrt a)))))

(def distance haversine)


;; Helpers

(defn cumsum
  "Compute the cumulative sum of the input sequence."
  [x]
  (reductions + x))

(defn sum
  "Compute the sum of the input sequence."
  [x]
  (reduce + x))

(defn div
  "Divide the whole collection by a number. Useful for normalization."
  [coll num]
  (mapv #(/ % num) coll))

(defn swap
  "Swaps the items in two indices in a vector."
  [v i1 i2]
  (assoc v i2 (v i1) i1 (v i2)))

(defn route-distance
  "Computes the total traveled distance given that particular order."
  [cities]
  (let [num-cities (count cities)]
    (reduce +
            (for
             [i (range num-cities)]
              (distance (cities i)
                        (cities (mod (inc i) num-cities)))))))

(defn make-route
  "Computes the route distance and fitness."
  [cities]
  (let [d (route-distance cities)
        f (/ 1 d)]
    {:cities cities :distance d :fitness f}))

(defn random-route
  "Generates a random route."
  [cities]
  (make-route (shuffle cities)))

(defn random-population
  "Generates a random population."
  [{:keys [cities population-size]}]
  (for [_ (range population-size)] (random-route cities)))

(defn assoc-index
  "Associates each element in the collection with its index in the `:index` keyword."
  [coll]
  (map-indexed (fn [index item] (assoc item :index index)) coll))

(defn rank-population
  "Ranks the population (in a descending order) by fitness."
  [population]
  (into [] (sort-by (comp - :fitness) (assoc-index population))))

(defn find-first
  "Returns the first item in a collection that matches the predicate `f`."
  [f coll]
  (first (filter f coll)))

(defn selection
  "Returns the indices of the selected individuals from this population."
  [ranked-population {:keys [elite-size]}]
  (let [fitnesses (mapv :fitness ranked-population)
        cum-sum (cumsum fitnesses)
        total-sum (sum fitnesses)
        cum-percentage (div cum-sum total-sum)
        enriched (mapv (fn [i cp] (assoc i :cp cp)) ranked-population cum-percentage)
        elite (map :index (subvec ranked-population 0 elite-size))
        remaining (- (count ranked-population) elite-size)
        others (for [_ (range remaining)]
                 (let [pick (rand)]
                   (:index (find-first (fn [i] (>= (:cp i) pick)) enriched))))]
    (concat elite others)))

(defn mating-pool
  "Constructs the mating pool from the population based on the selection result."
  [population selection-result]
  (for [r selection-result] (nth population r)))

(defn not-in?
  "Returns true if the element `elm` is not in the collection `coll`"
  [coll elm]
  (not-any? #(= elm %) coll))

(defn breed
  "Takes two individuals and breeds them, returning a new individual."
  [i1 i2]
  (let [num-cities (count (:cities i1))
        gene-1 (rand-int num-cities)
        gene-2 (rand-int num-cities)
        start (min gene-1 gene-2)
        end (max gene-1 gene-2)
        c1 (subvec (:cities i1) start end)
        c2 (filterv #(not-in? c1 %) (:cities i2))]
    (make-route (into [] (concat c1 c2)))))

(defn breed-population
  "Breeds the whole population, while keeping the elite."
  [population {:keys [elite-size]}]
  (let [remaining (- (count population) elite-size)
        pool (shuffle population)]
    (into []
          (concat
           (take elite-size population)
           (map (fn [e] (breed e (rand-nth pool))) (take remaining pool))))))

(defn mutate
  "Mutates an individual according to the mutation rate."
  [i mutation-rate]
  (let [route (:cities i)
        num-cities (count route)]
    (loop [r route
           current 0]
      (if (= current num-cities)
        (make-route r)
        (let [pick (rand)]
          (if (< pick mutation-rate)
            (recur (swap r current (rand-int num-cities)) (inc current))
            (recur r (inc current))))))))

(defn mutate-population
  "Does mutation on the entire population."
  [population {:keys [mutation-rate]}]
  (mapv #(mutate % mutation-rate) population))

(defn next-generation
  "Does a single step, evolves the current generation into the next generation."
  [population config]
  (let [ranked-population (rank-population population)
        selection-result (selection ranked-population config)
        pool (mating-pool population selection-result)
        children (breed-population pool config)]
    (mutate-population children config)))

(defn genetic-algorithm
  "The full algorithm. Evolves the first random population for a certain number of generations."
  [{:keys
    [num-generations] :as config}]
  (loop [population (random-population config)
         fitnesses (transient [])
         bar (pr/progress-bar num-generations)]
    (if (= (:progress bar) (:total bar))
      (do
        (pr/print (pr/done bar))
        (persistent! fitnesses))
      (do
        (pr/print bar)
        (let [best-so-far (first (rank-population population))]
          (recur
           (next-generation population config)
           (conj! fitnesses (:fitness best-so-far))
           (pr/tick bar)))))))

;; Plotting Helpers

(def layer [{:mark "line"
             :encoding {:y {:field "fitness" :type "quantitative" :title "Fitness"}}}
            {:mark {:type "rule" :opacity 1}
             :selection {:highlighted {:type "single"
                                       :on "mouseover"
                                       :encodings ["x"]
                                       :empty "none"
                                       :nearest true}}
             :encoding {:opacity {:condition {:selection "highlighted"
                                              :value 1}
                                  :value 0}}}
            {:mark {:type "point" :opacity 1 :size 100 :fill "white"}
             :encoding {"y" {:field "fitness" :type "quantitative"}
                        :opacity {:condition {:selection "highlighted"
                                              :value 1}
                                  :value 0}}}])

(def encoding {:x {:field "generation" :type "quantitative" :title "Generation"}
               :tooltip [{:field "generation" :type "quantitative" :title "Generation"}
                         {:field "fitness" :type "quantitative" :title "Fitness"}]})

(defn vega-data
  "Constructs Vega-Lite specification to be visualized."
  [fitnesses]
  (mapv
   (fn [g f] {:generation g :fitness f})
   (range 1 (inc (count fitnesses)))
   fitnesses))
