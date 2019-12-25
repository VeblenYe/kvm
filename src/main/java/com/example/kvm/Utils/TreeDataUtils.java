package com.example.kvm.Utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TreeDataUtils {
    private String title;
    private String id;
    private String field;
    private List<TreeDataUtils> children = new ArrayList<>();
    private String href;
    private boolean spread = false;
    private boolean checked = false;
    private boolean disable = false;
}
