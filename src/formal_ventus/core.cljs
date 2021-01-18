(ns formal-ventus.core
  (:require [formal.core :as f]
            [formal.util.component :as c]
            [ventus.tailwind-ui :as tw]
            [ventus.util.classes :refer [join-classes]]
            [utilis.js :as j]
            [utilis.types.number :refer [string->double string->long]]
            [inflections.core :as inflections]
            [reagent.core :as r]))

(defn labelize
  [label]
  (when label
    (inflections/titleize label)))

(defn error-typography
  [{:keys [prefix error]}]
  (let [error (or (-> error meta :human) error)
        ->error-string #(str (when prefix (str prefix ": ")) %)]
    (cond
      (and (sequential? error)
           (= 1 (count error)))
      [tw/typography {:color :error}
       (->error-string (first error))]

      (map? error)
      [:div
       (->> error
            (sort-by first)
            (map-indexed
             (fn [index [key error]]
               [error-typography
                {:key (str index "-" key)
                 :prefix (labelize key)
                 :error error}]))
            (doall))]

      :else
      [tw/typography {:color :error}
       (->error-string error)])))

(defn form-label
  [{:keys [props]}]
  (into [:label (merge {:class-name "block text-sm font-medium text-gray-700"} props)]
        (r/children (r/current-component))))

(defn form
  [{:keys [schema] :as props}]
  [:form
   [f/form (assoc props :namespace :ventus)]])

(defn form-item
  []
  (r/create-class
   {:render (fn [this]
              (let [{:keys [id value namespace error label label-position input-container-classes required]
                     :or {label-position :start}
                     :as props} (r/props this)
                    {:keys [input-id]} (r/state this)
                    label (or label (when id (labelize id)))
                    label (if required (str label "*") label)]
                [:div {:class-name "py-2"}
                 [:div {:class-name input-container-classes}
                  (when (and label (= label-position :start))
                    [form-label {:for input-id} label])
                  (when-let [input (first (r/children this))]
                    (c/assoc-prop input :id input-id))
                  (when (and label (= label-position :end))
                    [form-label {:for input-id} label])]
                 [error-typography {:error error}]]))
    :get-initial-state (fn [this]
                         {:input-id (str "ventus-input-" (gensym))})}))

(defn update-error
  [{:keys [required error value] :as props}]
  (if (or required (and (not required) value error))
    props
    (dissoc props :error)))

(defn string-input
  [{:keys [on-change] :as props}]
  (let [{:keys [error] :as props} (update-error props)]
    [form-item props
     [tw/input (-> props
                   (select-keys [:default-value :placeholder])
                   (assoc :type :text
                          :error (boolean error)
                          :on-change #(on-change (j/get-in % [:currentTarget :value]))))]]))

(defn integer-input
  [{:keys [on-change] :as props}]
  (let [{:keys [error] :as props} (update-error props)]
    [form-item props
     [tw/input (-> props
                   (select-keys [:default-value :placeholder])
                   (assoc :type :number
                          :error (boolean error)
                          :on-change #(on-change
                                       (string->long
                                        (j/get-in % [:currentTarget :value])))))]]))

(defn boolean-input
  [{:keys [on-change default-value] :as props}]
  (let [this (r/current-component)
        {:keys [checked] :or {checked default-value}} (r/state this)
        {:keys [error] :as props} (update-error props)]
    [form-item (assoc props
                      :label-position :end
                      :input-container-classes "flex justify-start items-center")
     [tw/switch {:checked (boolean checked)
                 :class-name :mr-1
                 :on-change #(let [checked (not checked)]
                               (on-change checked)
                               (r/set-state this {:checked checked}))}]]))

(defn enum-input
  [{:keys [on-change error children] :as props}]
  (let [this (r/current-component)
        {:keys [default-handled]} (r/state this)
        {:keys [error] :as props} (update-error props)
        options (map (fn [option]
                       (let [id (str option)]
                         {:id id :value option})) children)
        lookup (->> options
                    (map (fn [{:keys [id] :as option}]
                           [id option]))
                    (into {}))]
    (when (not default-handled)
      (when (not (:default-value props))
        (when-let [value (:value (first options))]
          (on-change value)))
      (r/set-state this {:default-handled true}))
    [form-item props
     [tw/select (-> props
                    (select-keys [:default-value])
                    (assoc :on-change #(on-change (:value (get lookup %)))))
      (->> options
           (map-indexed
            (fn [index {:keys [id value]}]
              [tw/select-option
               {:key (str "option-" index "-" id)
                :value id}
               (labelize value)]))
           (doall))]]))

(defn map-input
  [{:keys [index value error inputs id title subtitle]}]
  [:div {:class-name (join-classes
                      :p-4
                      (when (and index (pos? index)) :mt-2))}
   [:div
    (when title
      [tw/typography {:variant :h3
                      :text-size :lg
                      :classes {:text-color [:leadinfg-6 :font-medium :text-gray-900]}}
       title])
    (when subtitle
      [tw/typography {:variant :p
                      :text-size :sm
                      :classes {:text-color :text-gray-500}}
       subtitle])]
   (into
    [:div {:class-name (when (or title subtitle) :mt-2)}]
    (concat
     (->> inputs
          (sort-by (comp :index :props second))
          (map (fn [[_ {:keys [render props]}]]
                 [render props]))
          (doall))
     (when (and error (seq value))
       [[:div {:class-name :mt-2}
         [error-typography {:error error}]]])))])

(defn sequential-input
  [{:keys [inputs on-change value error] :as props}]
  (into [:div]
        (concat (->> inputs
                     (map (fn [{:keys [render props]}]
                            [render props]))
                     (doall))
                [[:button {:on-click #(on-change (conj value nil))} "Add"]]
                (when error
                  [[:div {:class-name :mt-2}
                    [error-typography {:error error}]]]))))

(f/reg-input :ventus/int integer-input)
(f/reg-input :ventus/number integer-input)
(f/reg-input :ventus/string string-input)
(f/reg-input :ventus/boolean boolean-input)
(f/reg-input :ventus/enum enum-input)
(f/reg-input :ventus/map map-input)
(f/reg-input :ventus/sequential sequential-input)
