package io.github.sunshinewzy.sunnybot.module.server.ping;

import java.util.List;

public class Description {

    private String text;
    
    private List<Extra> extra;
    

    public String getText() {
        return text;
    }

    public List<Extra> getExtra() {
        return extra;
    }
    
    
    public String getExtraContent() {
        StringBuilder builder = new StringBuilder();
        for(Extra extra : getExtra()) {
            builder.append(extra.getText());
        }
        return builder.toString();
    }
}
