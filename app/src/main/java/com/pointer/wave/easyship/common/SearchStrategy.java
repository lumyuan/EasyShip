package com.pointer.wave.easyship.common;

public interface SearchStrategy {
    int find(DocumentProvider var1, String var2, int var3, int var4, boolean var5, boolean var6);

    int findBackwards(DocumentProvider var1, String var2, int var3, int var4, boolean var5, boolean var6);

    int getProgress();

    Pair replaceAll(DocumentProvider var1, String var2, String var3, int var4, boolean var5, boolean var6);

    int wrappedFind(DocumentProvider var1, String var2, int var3, boolean var4, boolean var5);

    int wrappedFindBackwards(DocumentProvider var1, String var2, int var3, boolean var4, boolean var5);
}
