package Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {
    private String title, day, time, content, id, image;
    private boolean done, pin;

    public Note() {
    }

    public Note(String title, String day, String time, String content, String id, String image, boolean done, boolean pin) {
        this.title = title;
        this.day = day;
        this.time = time;
        this.content = content;
        this.id = id;
        this.image = image;
        this.done = done;
        this.pin = pin;
    }

    public Note(String title, String day, String time, String content) {
        this.title = title;
        this.day = day;
        this.time = time;
        this.content = content;
        this.id = "";
        this.pin = false;
        this.done = false;
        this.image = "";
    }

    protected Note(Parcel in) {
        title = in.readString ();
        day = in.readString ();
        time = in.readString ();
        content = in.readString ();
        id = in.readString ();
        image = in.readString ();
        done = in.readByte () != 0;
        pin = in.readByte () != 0;
    }

    public static final Creator<Note> CREATOR = new Creator<Note> () {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note (in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isCheck() {
        return done;
    }

    public void setCheck(boolean done) {
        this.done = done;
    }

    public boolean isPin() {
        return pin;
    }

    public void setPin(boolean pin) {
        this.pin = pin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString (title);
        parcel.writeString (day);
        parcel.writeString (time);
        parcel.writeString (content);
        parcel.writeString (id);
        parcel.writeString (image);
        parcel.writeByte ((byte) (done ? 1 : 0));
        parcel.writeByte ((byte) (pin ? 1 : 0));
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", day='" + day + '\'' +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                ", id='" + id + '\'' +
                ", image='" + image + '\'' +
                ", done=" + done +
                ", pin=" + pin +
                '}';
    }
}
