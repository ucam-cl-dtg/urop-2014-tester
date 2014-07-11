package uk.ac.cam.as2388.tick1;

import uk.ac.cam.as2388.tick4.ExceptionTest;
import uk.ac.cam.as2388.tick7.Strings;

public class PackedLong
{
    /*
        Unpack and return the nth bit from the packed number at index position;
        position counts from zero (representing the least significant bit)
        up to 63 (representing the most significant bit).
         */

    public static boolean get(long packed, int position)
    {
        //set "check" to equal 1 if the "position" bit in "packed" is set to 1
        long check=packed>>position & 1;
        return (check==1);
    }

    /*
        Set the nth bit in the packed number to the value given
        and return the new packed number
     */
    public static long set(long packed, int position, boolean value)
    {

        try{
            String x;

        } catch(Exception exception)
        {

        }

        if (value)
        {
             packed |= 1l<<position;
        }
        else
        {
            packed &= ~(1l<<position);
        }
        return packed;
    }
}
