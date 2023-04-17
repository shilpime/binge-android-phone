package com.tatasky.binge.ui.features.player.model;

import java.util.ArrayList;

final public class AudioLanguage {
        private ArrayList<String> list;
        private int defaultIndex = 0;

        public ArrayList<String> getList() {
                return list;
        }

        public void setList(ArrayList<String> list) {
                this.list = list;
        }

        public int getDefaultIndex() {
                return defaultIndex;
        }

        public void setDefaultIndex(int defaultIndex) {
                this.defaultIndex = defaultIndex;
        }
}
