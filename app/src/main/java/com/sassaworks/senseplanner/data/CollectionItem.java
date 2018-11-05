package com.sassaworks.senseplanner.data;

public interface CollectionItem {
  int TYPE_DATE = 1;
  int TYPE_DESC = 2;

  int getItemType();
  String getFormattedDate();

}