(ns formal-ventus.core
  (:require [formal.core :as f]
            [formal.util.component :as c]
            [ventus.tailwind-ui :as tw]
            [ventus.util.classes :refer [join-classes]]
            [utilis.js :as j]
            [utilis.types.number :refer [string->double string->long]]
            [inflections.core :as inflections]
            [reagent.core :as r]))

;; TODO
;; - handle required inputs (add asterisk to label)
;; - handle map-level errors properly
;; - finish handling sequential input

(defn error-typography
  [{:keys [error]}]
  (when error
    [tw/typography {:color :error}
     (str (or (when-let [error (-> error meta :human)]
                (cond
                  (and (sequential? error)
                       (= 1 (count error)))
                  (first error)

                  :else error))
              error))]))

(defn form-label
  [{:keys [props]}]
  (into [:label (merge {:class-name "block text-sm font-medium text-gray-700"} props)]
        (r/children (r/current-component))))

(defn form
  [{:keys [schema] :as props}]
  [f/form (assoc props :namespace :ventus)])

(defn labelize
  [label]
  (when label
    (inflections/titleize label)))

(defn form-item
  []
  (r/create-class
   {:render (fn [this]
              (let [{:keys [id namespace error label label-position input-container-classes]
                     :or {label-position :start}
                     :as props} (r/props this)
                    {:keys [input-id]} (r/state this)
                    label (or label (when id (labelize id)))]
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

(defn string-input
  [{:keys [on-change error] :as props}]
  [form-item props
   [tw/input (-> props
                 (select-keys [:default-value :placeholder])
                 (assoc :type :text
                        :error (boolean error)
                        :on-change #(on-change (j/get-in % [:currentTarget :value]))))]])

(defn integer-input
  [{:keys [on-change error] :as props}]
  [form-item props
   [tw/input (-> props
                 (select-keys [:default-value :placeholder])
                 (assoc :type :number
                        :error (boolean error)
                        :on-change #(on-change
                                     (string->long
                                      (j/get-in % [:currentTarget :value])))))]])

(defn boolean-input
  [{:keys [on-change default-value] :as props}]
  (let [this (r/current-component)
        {:keys [checked] :or {checked default-value}} (r/state this)]
    [form-item (assoc props
                      :label-position :end
                      :input-container-classes "flex justify-start items-center")
     [tw/switch {:checked (boolean checked)
                 :class-name :mr-1
                 :on-change #(let [checked (not checked)]
                               (on-change checked)
                               (r/set-state this {:checked checked}))}]]))

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
     (map second inputs)
     (when (and error (seq value))
       [[:div {:class-name :mt-2}
         [error-typography {:error error}]]])))])

(defn sequential-input
  [{:keys [inputs on-change value error] :as props}]
  (into [:div]
        (concat inputs
                [[:button {:on-click #(on-change (conj value nil))} "Add"]]
                (when error
                  [[:div {:class-name :mt-2}
                    [error-typography {:error error}]]]))))

(f/reg-input :ventus/integer integer-input)
(f/reg-input :ventus/number integer-input)
(f/reg-input :ventus/string string-input)
(f/reg-input :ventus/boolean boolean-input)
(f/reg-input :ventus/map map-input)
(f/reg-input :ventus/sequential sequential-input)
