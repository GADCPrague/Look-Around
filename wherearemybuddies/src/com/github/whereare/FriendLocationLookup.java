package com.github.whereare;

import android.content.ContentUris;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.util.Pair;

public class FriendLocationLookup {

  /**
   * Contact list query result cursor.
   */
  public static Cursor cursor;

  /**
   * Contact's home address query result cursor.
   */
  public static Cursor addressCursor;

  /**
   * Return a hash of contact names and the reverse-geocoded location of their
   * home addresses.
   * 
   * @param context
   *          Calling application's context
   * @return Hash of contact names with their physical locations
   */
  public static Map<String, Pair<Location, Uri>> getFriendLocations(Context context) {
    HashMap<String, Pair<Location, Uri>> result = new HashMap<String, Pair<Location, Uri>>();

    // Return a query result of all the people in the contact list.
    cursor = context.getContentResolver().query(People.CONTENT_URI, null, null,
            null, null);

    // Use the convenience properties to get the index of the columns
    int nameIdx = cursor.getColumnIndexOrThrow(People.NAME);
    int personID = cursor.getColumnIndexOrThrow(People._ID);

    if (cursor.moveToFirst())
      do {
        // Extract the name.
        String name = cursor.getString(nameIdx);
        int idx = name != null ? name.indexOf(' ') : 0;
        if (idx > 0) {
            if (name.length() > idx + 1) {
                name = name.substring(0, idx + 2) + '.';
            } else {
                name = name.substring(0, idx);
            }
        }
        String id = cursor.getString(personID);

        // Extract the address.
        String where = Contacts.ContactMethods.PERSON_ID + " == " + id
                + " AND " + Contacts.ContactMethods.KIND + " == "
                + Contacts.KIND_POSTAL;

        addressCursor = context.getContentResolver().query(
                Contacts.ContactMethods.CONTENT_URI, null, where, null, null);

        // Extract the postal address from the cursor
        int postalAddress = addressCursor
                .getColumnIndexOrThrow(Contacts.ContactMethodsColumns.DATA);
        String address = "";
        if (addressCursor.moveToFirst())
          address = addressCursor.getString(postalAddress);
        addressCursor.close();

        // Reverse geocode the postal address to get a location.
        Location friendLocation = new Location("reverseGeocoded");

        if (address != null && !"".equals(address)) {
          Log.d("whereare", "Finding coords of " + address + ".");
          Geocoder geocoder = new Geocoder(context, Locale.getDefault());
          try {
            List<Address> addressResult = geocoder.getFromLocationName(address,
                    1);
            if (!addressResult.isEmpty()) {
              Address resultAddress = addressResult.get(0);
              friendLocation.setLatitude(resultAddress.getLatitude());
              friendLocation.setLongitude(resultAddress.getLongitude());
            }
          } catch (IOException e) {
            Log.d("whereare", "Contact Location Lookup Failed", e);
          }
          
          // Populate the result hash
          result.put(name, 
                  Pair.create(friendLocation, ContentUris.withAppendedId(People.CONTENT_URI, cursor.getLong(personID))));
          Log.d("whereare", "Added location of " + name + ", " + friendLocation);
        }

        if (result.size() > 10) {
          break;
        }

      } while (cursor.moveToNext());

    // Cleanup the cursor.
    cursor.close();

    return result;
  }
}
