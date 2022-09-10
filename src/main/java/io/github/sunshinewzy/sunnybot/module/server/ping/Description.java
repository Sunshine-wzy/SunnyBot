package io.github.sunshinewzy.sunnybot.module.server.ping;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Description {

    private String text;
    
    private List<Extra> extras;
    

    public String getText() {
        return text;
    }

    public List<Extra> getExtras() {
        return extras;
    }
    
    @NotNull
    public String getExtraContent() {
        StringBuilder builder = new StringBuilder();
        List<Extra> list = getExtras();
        if(list != null) {
            for(Extra extra : list) {
                if(extra != null) {
                    builder.append(extra.getText());
                }
            }
        }
        return builder.toString();
    }
}
