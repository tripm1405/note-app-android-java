package project.note;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import Model.Note;

public class NoteAction {

    public static void readPinNote(ArrayList<Note> list){
        if(list.size () > 0){
            if(list.get (0).isPin ())
                return;
        }

        for(int i = 1; i < list.size (); i++){
            if(list.get (i).isPin ()){
                permutationNote(list.get (0), list.get (i));
                return;
            }
        }

        sortDayTime (list, false);
    }

    public static void removePinNote (ArrayList<Note> list, Note note){
        note.setPin (false);
        updatePinNote (note);
        int index = 0;
        for(int i = 0; i < list.size (); i++){
            if(list.get (i).equals (note)){
                index = i;
                break;
            }
        }

        for (int i = index; i < list.size (); i++) {
            if(list.get (i+1).isPin ())
                permutationNote(list.get (i), list.get (i+1));
            else
                break;
        }
        sortDayTime (list, false);
    }

    public static void permutationNote(Note note1, Note note2){
        Note tmp = new Note (note1.getTitle (), note1.getDay (), note1.getTime (), note1.getContent ()
                            , note1.getId (), note1.getImage (), note1.isCheck (), note1.isPin ());

        note1.setTitle (note2.getTitle ());
        note1.setDay (note2.getDay ());
        note1.setTime (note2.getTime ());
        note1.setContent (note2.getContent ());
        note1.setId (note2.getId ());
        note1.setImage (note2.getImage ());
        note1.setCheck (note2.isCheck ());
        note1.setPin (note2.isPin ());

        note2.setTitle (tmp.getTitle ());
        note2.setDay (tmp.getDay ());
        note2.setTime (tmp.getTime ());
        note2.setContent (tmp.getContent ());
        note2.setId (tmp.getId ());
        note2.setImage (tmp.getImage ());
        note2.setCheck (tmp.isCheck ());
        note2.setPin (tmp.isPin ());
    }

    public static void updatePinNote(Note note){
        HashMap<String, Object> hmPinNote = new HashMap<> ();
        hmPinNote.put ("Pin", note.isPin ());
        ListNote.ref.document (note.getId ()).update (hmPinNote)
        .addOnSuccessListener (unused -> Log.e ("TAG", "updatePinNote success."))
        .addOnFailureListener (e -> Log.e ("TAG", "updatePinNote Failure"));
    }

    public static void sortDayTime(ArrayList<Note> list, boolean pin){
        boolean isSort = false;
        boolean sort = false;
        int startSort = 0;
        int size = list.size ();

        if(!pin){
            for (int i = 0; i < size; i++) {
                if(list.get (i).isPin ())
                    continue;
                else {
                    startSort = i;
                    break;
                }
            }
        }


        while (!sort) {
            isSort = false;
            for (int j = startSort; j < size-1;j++) {
                int cDay = compareDay (list.get (j).getDay (), list.get (j+1).getDay ());

                if(cDay == -1){
                    permutationNote (list.get (j), list.get (j+1));
                    isSort = true;
                }
                else if(cDay == 1){
                    continue;
                }
                else {
                    int cTime = compareTime (list.get (j).getTime (), list.get (j+1).getTime ());
                    if(cTime == -1){
                        permutationNote (list.get (j), list.get (j+1));
                        isSort = true;
                    }else{
                        continue;
                    }
                }
            }
            if(!isSort)
                sort = true;
            else
                size -= 1;
        }
    }

    public static int compareDay(String day1, String day2){
        String[] strDay1 = day1.split ("/");
        day1 = strDay1[0] + strDay1[1] + strDay1[2];
        int intDay1 = Integer.parseInt (day1);

        String[] strDay2 = day2.split ("/");
        day2 = strDay2[0] + strDay2[1] + strDay2[2];
        int intDay2 = Integer.parseInt (day2);

        if(intDay1 > intDay2)
            return 1;
        else if(intDay1 < intDay2)
            return -1;
        else
            return 0;
    }

    public static int compareTime(String time1, String time2){
        String[] strTime1 = time1.split (":");
        time1 = strTime1[0] + strTime1[1];
        int intTime1 = Integer.parseInt (time1);

        String[] strTime2 = time2.split (":");
        time2 = strTime2[0] + strTime2[1];
        int intTime2 = Integer.parseInt (time2);

        if(intTime1 > intTime2)
            return 1;
        else if(intTime1 > intTime2)
            return -1;
        else
            return 0;
    }

    public static void pinNote(ArrayList<Note> list, Note pNote, Context context){
        if(pNote.isPin ())
            Toast.makeText (context, "Pinned", Toast.LENGTH_SHORT).show ();
        else{
            pNote.setPin (true);
            updatePinNote(pNote);
        }

        ArrayList<Note> tmpList = new ArrayList<> ();
        for (Note tmpNote : list) {
            if(tmpNote.isPin ())
                tmpList.add (tmpNote);
        }

        sortDayTime (tmpList, true);
        for (int i = 0; i < tmpList.size (); i++) {
            if(list.get (i).equals (tmpList.get (i)))
                continue;
            else{
                for (Note note : list) {
                    if(note.getId ().equals (tmpList.get (i).getId ())){
                        permutationNote(list.get (i), note);
                        break;
                    }

                }
            }
        }

        sortDayTime (list, false);
    }
}
