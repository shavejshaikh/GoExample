package com.hadoop.mapreduce.example;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

   public class Top5_categories {

    public static class Map extends Mapper<LongWritable, Text, Text,
IntWritable> {

       private Text category = new Text();
       private final static IntWritable one = new IntWritable(1);
       public void map(LongWritable key, Text value, Context context )
throws IOException, InterruptedException {
           String line = value.toString();
           String str[]=line.split("\t");

          if(str.length > 5){
                category.set(str[3]);
          }

      context.write(category, one);
      }

    }

    public static class Reduce extends Reducer<Text, IntWritable,
Text, IntWritable> {

       public void reduce(Text key, Iterable<IntWritable> values,
Context context)
         throws IOException, InterruptedException {
           int sum = 0;
           for (IntWritable val : values) {

               sum += val.get();
           }
           System.out.println("Person watches"+key+"No of people "+sum);
           context.write(key, new IntWritable(sum));
       }
    }

    public static void main(String[] args) throws Exception {
       Configuration conf = new Configuration();

       // @SuppressWarnings("deprecation")
        Job job = Job.getInstance(conf, "categories");
        job.setJarByClass(Top5_categories.class);
           
        Path inputPath = new Path("hdfs://127.0.0.1:9000/input/youtubedata.txt");
   		Path outputPath = new Path("hdfs://127.0.0.1:9000/output/recom");


       job.setMapOutputKeyClass(Text.class);
       job.setMapOutputValueClass(IntWritable.class);
      //job.setNumReduceTasks(0);
       job.setOutputKeyClass(Text.class);
       job.setOutputValueClass(IntWritable.class);

       job.setMapperClass(Map.class);
       job.setReducerClass(Reduce.class);

       job.setInputFormatClass(TextInputFormat.class);
       job.setOutputFormatClass(TextOutputFormat.class);

       FileInputFormat.setInputPaths(job, inputPath);
       FileOutputFormat.setOutputPath(job,outputPath);
       
       FileSystem hdfs = FileSystem.get(URI.create("hdfs://127.0.0.1:9000"), conf);
       
       if (hdfs.exists(outputPath))
        hdfs.delete(outputPath, true);

   	   System.exit(job.waitForCompletion(true) ? 0 : 1);
      
    }

  }
