package com.pointer.wave.easyship.android;

public interface TextChangeListener
{

    void onNewLine(String c, int _caretPosition, int p2);

    void onDel(CharSequence text, int _caretPosition, int newCursorPosition);

    void onAdd(CharSequence text, int _caretPosition, int newCursorPosition);

}
