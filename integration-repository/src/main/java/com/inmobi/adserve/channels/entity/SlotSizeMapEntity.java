package com.inmobi.adserve.channels.entity;

import java.awt.Dimension;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

/**
 * Created by anshul.soni on 27/11/14.
 */
@Getter
public class SlotSizeMapEntity implements IdentifiableEntity<Short> {


        private final Short slotId;
        private final Dimension dimension;
        private final Timestamp modifiedOn;

        public SlotSizeMapEntity(final Builder builder) {
            slotId = builder.slotId;
            dimension = builder.dimension;
            modifiedOn = builder.modifiedOn;
        }

        public static Builder newBuilder() {
            return new Builder();
        }



        @Setter
        public static class Builder {
            private Short slotId;
            private Dimension dimension;
            private Timestamp modifiedOn;

            public SlotSizeMapEntity build() {
                return new SlotSizeMapEntity(this);
            }

        }

        @Override
        public Short getId() {
            return slotId;
        }

        @Override
        public String getJSON() {
            return String
                    .format("{\"slotId\":\"%s\",\"dimension\":%s,\"modifiedOn\":\"%s\"}",
                            slotId, dimension, modifiedOn);
        }



}
