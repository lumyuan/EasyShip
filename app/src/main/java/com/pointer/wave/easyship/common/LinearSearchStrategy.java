package com.pointer.wave.easyship.common;

public class LinearSearchStrategy implements SearchStrategy {
    private int _unitsDone = 0;

    public LinearSearchStrategy() {
    }

    protected boolean equals(DocumentProvider var1, String var2, int var3, boolean var4) {
        if (var1.docLength() - var3 < var2.length()) {
            var4 = false;
        } else {
            int var5 = 0;

            while(true) {
                if (var5 >= var2.length()) {
                    var4 = true;
                    break;
                }

                if (var4 && var2.charAt(var5) != var1.charAt(var5 + var3)) {
                    var4 = false;
                    break;
                }

                if (!var4 && Character.toLowerCase(var2.charAt(var5)) != Character.toLowerCase(var1.charAt(var5 + var3))) {
                    var4 = false;
                    break;
                }

                ++var5;
            }
        }

        return var4;
    }

    @Override
    public int find(DocumentProvider var1, String var2, int var3, int var4, boolean var5, boolean var6) {
        int var7 = var3;
        if (var2.length() == 0) {
            var3 = -1;
        } else {
            var3 = var3;
            if (var7 < 0) {
                TextWarriorException.fail("TextBuffer.find: Invalid start position");
                var3 = 0;
            }

            var7 = var4;
            if (var4 > var1.docLength()) {
                TextWarriorException.fail("TextBuffer.find: Invalid end position");
                var7 = var1.docLength();
            }

            for(var4 = Math.min(var7, var1.docLength() - var2.length() + 1); var3 < var4 && (!this.equals(var1, var2, var3, var5) || var6 && !this.isSandwichedByWhitespace(var1, var3, var2.length())); ++this._unitsDone) {
                ++var3;
            }

            if (var3 >= var4) {
                var3 = -1;
            }
        }

        return var3;
    }

    @Override
    public int findBackwards(DocumentProvider var1, String var2, int var3, int var4, boolean var5, boolean var6) {
        int var7 = var3;
        var3 = var4;
        if (var2.length() == 0) {
            var3 = -1;
        } else {
            int var8 = var7;
            if (var7 >= var1.docLength()) {
                TextWarriorException.fail("Invalid start position given to TextBuffer.find");
                var8 = var1.docLength() - 1;
            }

            var4 = var4;
            if (var3 < -1) {
                TextWarriorException.fail("Invalid end position given to TextBuffer.find");
                var4 = -1;
            }

            for(var3 = Math.min(var8, var1.docLength() - var2.length()); var3 > var4 && (!this.equals(var1, var2, var3, var5) || var6 && !this.isSandwichedByWhitespace(var1, var3, var2.length())); --var3) {
            }

            if (var3 <= var4) {
                var3 = -1;
            }
        }

        return var3;
    }

    @Override
    public int getProgress() {
        return this._unitsDone;
    }

    protected boolean isSandwichedByWhitespace(DocumentProvider var1, int var2, int var3) {
        Language var4 = Lexer.getLanguage();
        boolean var5;
        if (var2 == 0) {
            var5 = true;
        } else {
            var5 = var4.isWhitespace(var1.charAt(var2 - 1));
        }

        var2 += var3;
        boolean var6;
        if (var2 == var1.docLength()) {
            var6 = true;
        } else {
            var6 = var4.isWhitespace(var1.charAt(var2));
        }

        if (var5 && var6) {
            var5 = true;
        } else {
            var5 = false;
        }

        return var5;
    }

    @Override
    public Pair replaceAll(DocumentProvider var1, String var2, String var3, int var4, boolean var5, boolean var6) {
        int var7 = 0;
        int var8 = var4;
        this._unitsDone = 0;
        char[] var9 = var3.toCharArray();
        var4 = this.find(var1, var2, 0, var1.docLength(), var5, var6);
        long var10 = System.nanoTime();
        var1.beginBatchEdit();

        while(var4 != -1) {
            var1.deleteAt(var4, var2.length(), var10);
            var1.insertBefore(var9, var4, var10);
            int var12 = var8;
            if (var4 < var8) {
                var12 = var8 + (var3.length() - var2.length());
            }

            ++var7;
            this._unitsDone += var2.length();
            var4 = this.find(var1, var2, var4 + var3.length(), var1.docLength(), var5, var6);
            var8 = var12;
        }

        var1.endBatchEdit();
        return new Pair(var7, Math.max(var8, 0));
    }

    @Override
    public int wrappedFind(DocumentProvider var1, String var2, int var3, boolean var4, boolean var5) {
        int var6 = this.find(var1, var2, var3, var1.docLength(), var4, var5);
        int var7 = var6;
        if (var6 < 0) {
            var7 = this.find(var1, var2, 0, var3, var4, var5);
        }

        return var7;
    }

    @Override
    public int wrappedFindBackwards(DocumentProvider var1, String var2, int var3, boolean var4, boolean var5) {
        int var6 = this.findBackwards(var1, var2, var3, -1, var4, var5);
        int var7 = var6;
        if (var6 < 0) {
            var7 = this.findBackwards(var1, var2, var1.docLength() - 1, var3, var4, var5);
        }

        return var7;
    }
}