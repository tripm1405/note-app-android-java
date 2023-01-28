package project.note;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class User implements Parcelable {
  private String username;
  private String password;
  private String type;
  private boolean activated;

  public User() {

  }

  public User(String username, String password, String type, boolean activated) {
    this.username = username;
    this.password = password;
    this.type = type;
    this.activated = activated;
  }

  protected User(Parcel in) {
    username = in.readString();
    password = in.readString();
    activated = in.readByte() != 0;
  }

  public static final Creator<User> CREATOR = new Creator<User>() {
    @Override
    public User createFromParcel(Parcel in) {
      return new User(in);
    }

    @Override
    public User[] newArray(int size) {
      return new User[size];
    }
  };

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(boolean activated) {
    this.activated = activated;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(this.username);
    parcel.writeString(this.password);
    parcel.writeBoolean(this.activated);
  }
}
