package com.brennaswitzer.cookbook.services;

import com.github.difflib.DiffUtils;
import com.github.difflib.unifieddiff.UnifiedDiff;
import com.github.difflib.unifieddiff.UnifiedDiffFile;
import com.github.difflib.unifieddiff.UnifiedDiffWriter;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DiffService {

    public String diffLinesToPatch(List<String> left,
                                   List<String> right) {
        try {
            return diffLinesToPatchInternal(withoutNulls(left),
                                            withoutNulls(right));
        } catch (Exception e) {
            log.warn(String.format("Failed to diff %s and %s",
                                   left,
                                   right),
                     e);
            return "";
        }
    }

    @Nonnull
    private static List<String> withoutNulls(List<String> left) {
        return left.stream().filter(Objects::nonNull).toList();
    }

    private String diffLinesToPatchInternal(List<String> left,
                                            List<String> right) {
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
        return sb.substring(idx + 1);
    }

}
