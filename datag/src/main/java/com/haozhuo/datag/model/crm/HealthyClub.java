package com.haozhuo.datag.model.crm;

public class HealthyClub {
        private int infoId;
        private String title;
        private String imageUrl;

        public HealthyClub(int infoId, String title, String imageUrl) {
            this.infoId = infoId;
            this.title = title;
            this.imageUrl = imageUrl;
        }

        public int getInfoId() {

            return infoId;
        }

        public void setInfoId(int infoId) {
            this.infoId = infoId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

}
