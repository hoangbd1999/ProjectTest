package com.elcom.metacen.content.schedule;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();
        int m = input.nextInt();
        Map<Integer, Set<Integer>> map = new HashMap<>();
        for (int i=0;i<m;i++
             ) {
            int n1 = input.nextInt();
            int n2 = input.nextInt();
            if(map.get(n1)!=null){
                Set<Integer> relation = map.get(n1);
                relation.add(n2);
                map.put(n1,relation);
            }else {
                Set<Integer> relation = new HashSet<>();
                relation.add(n2);
                map.put(n1,relation);
            }
            if(map.get(n2)!=null){
                Set<Integer> relation = map.get(n2);
                relation.add(n1);
                map.put(n2,relation);
            }else {
                Set<Integer> relation = new HashSet<>();
                relation.add(n1);
                map.put(n2,relation);
            }
        }
        int max =1;

        for (int i=0;i<n;i++){
            if(map.get(i)!=null){
                Set<Integer> relation = map.get(i);
                int next =i+1;
                int count =1;


                while (relation.contains(next)){
                    List<Integer> list= new ArrayList<>();
                    for (int j=i;j<=next;j++
                    ) {
                        list.add(j);
                    }
                    boolean check =true;
                    for (int j=i;j<=next;j++
                         ) {
                        if(map.get(j)!=null){
                            Set<Integer> relation2 = map.get(j);
                            List<Integer> listRemove = new ArrayList<>();
                            listRemove.add(j);
                            list.removeAll(listRemove);
                            if(!relation2.containsAll(list)){
                                check=false;
                                break;
                            }
                            list.addAll(listRemove);
                        }else {
                            check=false;
                            break;
                        }
                    }
                    if(check){
                        count++;
                        if(count>max){
                            max=count;
                        }
                    }else {
                        break;
                    }
                    next++;

                }
            }
        }
        System.out.println(max);
    }
}
