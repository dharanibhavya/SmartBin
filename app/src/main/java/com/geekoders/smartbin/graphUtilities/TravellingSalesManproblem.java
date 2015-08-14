package com.geekoders.smartbin.graphUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kovbh01 on 8/13/2015.
 */

public class TravellingSalesManproblem {
    int adj[][], n, npow, g[][], p[][];

    List<Integer> paths = new ArrayList<>();

    public TravellingSalesManproblem(int n, int adj[][]) {
        this.n = n;
        this.adj = adj;
        npow = (int) Math.pow(2, n);
        g = new int[n][npow];
        p = new int[n][npow];
    }

    private int compute(int start, int set) {
        int masked, mask, result = Integer.MAX_VALUE, temp, i;// result stores the minimum
        if (g[start][set] != -1)// memoization DP top-down,check for repeated
            // subproblem
            return g[start][set];
        for (i = 0; i < n; i++) { // npow-1 because we always exclude "home"
            // vertex from our set
            mask = (npow - 1) - (1 << i);// remove ith vertex from this set
            masked = set & mask;
            if (masked != set)// in case same set is generated(because ith
            // vertex was not present in the set hence we
            // get the same set on removal) eg 12&13=12
            {
                temp = adj[start][i] + compute(i, masked);// compute the removed
                // set
                if (temp < result) {
                    result = temp;
                    p[start][set] = i;// removing ith vertex gave us minimum
                }
            }
        }
        return g[start][set] = result;// return minimum
    }

    private void getpath(int start, int set) {
        if (p[start][set] == -1)
            return;// reached null set
        int x = p[start][set];
        int mask = (npow - 1) - (1 << x);
        int masked = set & mask;// remove p from set
        paths.add(x);
//        System.out.print(x + " ");
        getpath(x, masked);
    }

    public List<Integer> execute() {
        int i, j;
        // g(i,S) is length of shortest path starting at i visiting all vertices
        // in S and ending at 1
        for (i = 0; i < n; i++)
            for (j = 0; j < npow; j++)
                g[i][j] = p[i][j] = -1;
        for (i = 0; i < n; i++)
            g[i][0] = adj[i][0];// g(i,nullset)= direct edge between (i,1)
        int result = compute(1, npow - 2);// npow-2 to exclude our
        // "home" vertex
//        System.out.println("Tour cost: " + result);
//        System.out.println("Tour path: ");
        paths.add(0);
        getpath(1, npow - 2);
        return paths;
    }
}


