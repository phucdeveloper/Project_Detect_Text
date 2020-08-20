//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ximgproc;

// C++: class SelectiveSearchSegmentationStrategyColor
//javadoc: SelectiveSearchSegmentationStrategyColor

public class SelectiveSearchSegmentationStrategyColor extends SelectiveSearchSegmentationStrategy {

    protected SelectiveSearchSegmentationStrategyColor(long addr) { super(addr); }

    // internal usage only
    public static SelectiveSearchSegmentationStrategyColor __fromPtr__(long addr) { return new SelectiveSearchSegmentationStrategyColor(addr); }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // native support for java finalize()
    private static native void delete(long nativeObj);

}
