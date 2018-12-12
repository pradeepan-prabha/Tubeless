package com.techmind.tubeless;

public class MyCustomObject {
    public interface MyCustomObjectListener {
        // These methods are the different events and need to pass relevant arguments with the event
        public void onDataLoad(String title, String type);
    }
    // static variable single_instance of type Singleton
    public static MyCustomObject single_instance=null;
    public MyCustomObjectListener listener;

    public static MyCustomObject getInstance()
    {
        if (single_instance == null)
            single_instance = new MyCustomObject();

        return single_instance;
    }
    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
    public void setCustomObjectListener(MyCustomObjectListener listener) {
        this.listener = listener;
    }
}
