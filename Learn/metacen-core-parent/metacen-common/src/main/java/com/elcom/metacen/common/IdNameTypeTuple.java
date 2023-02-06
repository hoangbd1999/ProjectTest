package com.elcom.metacen.common;

import lombok.*;
import java.io.Serializable;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IdNameTypeTuple implements Serializable {

    private String id;
    private String name;
    private String type;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        IdNameValueTuple other = (IdNameValueTuple) obj;
        return this.getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
