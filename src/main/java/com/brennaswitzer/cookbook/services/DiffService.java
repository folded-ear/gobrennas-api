package com.brennaswitzer.cookbook.services;

import com.github.difflib.DiffUtils;
import com.github.difflib.unifieddiff.UnifiedDiff;
import com.github.difflib.unifieddiff.UnifiedDiffFile;
import com.github.difflib.unifieddiff.UnifiedDiffWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DiffService {

    public String diffLinesToPatch(List<String> left, List<String> right) {
        var patch = DiffUtils.diff(left, right);
        var udf = UnifiedDiffFile.from(null, null, patch);
        var ud = UnifiedDiff.from(null, null, udf);
        var sb = new StringBuilder();
        try {
            UnifiedDiffWriter.write(ud, s -> left, l -> sb.append(l).append('\n'), 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var idx = sb.indexOf("\n");
        return sb.substring(idx + 1).trim();
    }

}
