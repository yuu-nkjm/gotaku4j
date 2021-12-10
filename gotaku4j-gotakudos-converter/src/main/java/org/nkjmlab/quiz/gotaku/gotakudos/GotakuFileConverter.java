package org.nkjmlab.quiz.gotaku.gotakudos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.nkjmlab.quiz.gotaku.util.HexUtils;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.function.Try;


/**
 *
 * <pre>
 * 5tq/
 *      正統派クイズ2000題/
 *              5DATA.5TQ
 *              toc.txt (optional)
 *              gaiji.json (optional)

 *      戦国武将/
 *              SENGOKU.5TQ
 *              toc.txt (optional)
 *              gaiji.json (optional)
 * </pre>
 *
 * @author nkjm
 *
 */
public class GotakuFileConverter {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("usege: GotakuFileConverter 5tqSrcDir, outputDir");
      return;
    }
    new GotakuFileConverter().convertToTextFile(new File(args[0]), new File(args[1]));
  }

  public GotakuFileConverter() {}

  /**
   * 与えられたディレクトリ以下のごたくどす用クイズデータを，ジャンル毎に分割したテキストファイルに書き出す．すでにファイルが存在している場合は上書き保存されることに注意すること．
   *
   * 入力例
   *
   * <pre>
   *      正統派クイズ2000題/
   *              5DATA.5TQ
   *              toc.txt (optional)
   *              gaiji.json (optional)
   * </pre>
   *
   * 出力例
   *
   * <pre>
   *  00行目: ジャンル名
   *  01行目: 問題文
   *  02行目: 選択肢1 (正答)
   *  03行目: 選択肢2
   *  04行目: 選択肢3
   *  05行目: 選択肢4
   *  06行目: 選択肢5
   *  07行目:
   *  08行目: 問題文
   *  09行目: 選択肢1 (正答)
   *  10行目: 選択肢2
   *  …
   * </pre>
   *
   * @param _5tqSrcDir
   * @param outputDir
   */
  public void convertToTextFile(File _5tqSrcDir, File outputDir) {
    if (!outputDir.isDirectory()) {
      throw new RuntimeException("output target [" + outputDir + "] should be a directory.");
    }
    GotakuQuizBook book = parse(_5tqSrcDir);
    List<GotakuQuizGenre> genres = book.getGenres();
    for (int i = 0; i < genres.size(); i++) {
      GotakuQuizGenre genre = genres.get(i);
      File outputFile = new File(outputDir, i + "-5DATA" + ".txt");
      try (BufferedWriter bw =
          Files.newBufferedWriter(outputFile.toPath(), StandardOpenOption.CREATE)) {
        bw.write(genre.getGenreName());
        bw.newLine();
        for (GotakuQuiz quiz : genre.getQuizzes()) {
          bw.write(quiz.getQuestion());
          bw.newLine();
          for (String selection : quiz.getSelections()) {
            bw.write(selection);
            bw.newLine();
          }
          bw.newLine();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 子のディレクトリに含まれるごたくどす用クイズデータを読み込み，子ディレクトリ名をキー，{@link GotakuQuizBook}オブジェクトを値としたMapに変換する．
   *
   * 例えば，以下の様なディレクトリ構成となっている場合に，<code>5tq</code>を引数に指定すると，「正統派クイズ2000題」と「戦国武将」のクイズデータが読み込まれる．
   *
   * <pre>
   * 5tq/
   *      正統派クイズ2000題/
   *              5DATA.5TQ
   *              toc.txt (optional)
   *      戦国武将/
   *              SENGOKU.5TQ
   *              toc.txt (optional)
   * </pre>
   *
   * @param _5tqsRootDir
   * @return
   */
  public Map<String, GotakuQuizBook> parseAll(File _5tqsRootDir) {
    return Arrays.stream(_5tqsRootDir.listFiles()).filter(subDir -> subDir.isDirectory())
        .map(subDir -> parse(subDir))
        .collect(Collectors.toMap(book -> book.getBookName(), book -> book));
  }


  public GotakuQuizBook parse(File _5tqDir) {
    String bookName = _5tqDir.getName();
    List<String> toc = Try.getOrElse(() -> {
      File tocFile = new File(_5tqDir, "toc.txt");
      return tocFile.exists() ? Files.readAllLines(tocFile.toPath()) : Collections.emptyList();
    }, Collections.emptyList());
    File _5tqFile = Arrays.stream(_5tqDir.listFiles())
        .filter(fileInSubDir -> fileInSubDir.getName().toLowerCase().endsWith(".5tq")).findFirst()
        .get();

    Map<String, Object> gaijiMap = Try.getOrElse(() -> {
      File gaijiFile = new File(_5tqDir, "gaiji.json");
      return gaijiFile.exists() ? JacksonMapper.getDefaultMapper().toMap(gaijiFile)
          : Collections.emptyMap();
    }, Collections.emptyMap());


    return parse(bookName, toc, _5tqFile, gaijiMap.entrySet().stream()
        .collect(Collectors.toMap(en -> en.getKey(), en -> en.getValue().toString())));
  }

  /**
   *
   * @param bookName
   * @param toc
   * @param _5tqFile
   * @return
   *
   */
  public GotakuQuizBook parse(String bookName, List<String> toc, File _5tqFile,
      Map<String, String> gaijiMap) {

    try (FileInputStream is = new FileInputStream(_5tqFile)) {
      List<Header> headers = readHeaders(is);
      List<GotakuQuizGenre> genres = new ArrayList<>(headers.size());

      for (int h = 0; h < headers.size(); h++) {
        Header header = headers.get(h);
        List<GotakuQuiz> quizzes = readQuizzez(is, header, gaijiMap);
        genres.add(new GotakuQuizGenre(header.title, quizzes));
      }
      return new GotakuQuizBook(bookName, toc, genres);
    } catch (IOException e) {
      throw Try.rethrow(e);
    }
  }

  private static List<Header> readHeaders(FileInputStream is) {
    final int GENRE_NUM = 8;
    List<Header> headers = new ArrayList<>(GENRE_NUM);
    for (int genre = 0; genre < GENRE_NUM; genre++) {
      String title = readDoubleByteAsStringAndTrim(is, 16);
      readAsShort(is); // pass
      int size = (int) readAsShort(is);
      int skip = readAsShort(is);
      readDoubleByteAsStringAndTrim(is, 12); // playerData
      readDoubleByteAsStringAndTrim(is, 8); // magic
      readDoubleByteAsStringAndTrim(is, 214); // padding
      headers.add(new Header(title, size, skip));
    }
    return headers;
  }

  private List<GotakuQuiz> readQuizzez(FileInputStream is, Header header,
      Map<String, String> gaijiMap) {
    final int SELECTION_NUM = 5;
    List<GotakuQuiz> quizzes = new ArrayList<>();
    for (int i = 0; i < header.size; i++) {
      String message = readDoubleByteAsStringAndTrimWithMask(is, 116, gaijiMap);
      List<String> selections = new ArrayList<>(SELECTION_NUM);
      for (int s = 0; s < SELECTION_NUM; s++) {
        selections.add(readDoubleByteAsStringAndTrimWithMask(is, 28, gaijiMap));
      }
      GotakuQuiz gotakuQuiz = new GotakuQuiz(message, selections.get(0), selections);
      quizzes.add(gotakuQuiz);
    }
    return quizzes;
  }


  private static final String FROM_ENCODE = "SHIFT-JIS";

  private String readDoubleByteAsStringAndTrimWithMask(FileInputStream is, int length,
      Map<String, String> gaijiMap) {
    return Try.getOrElseThrow(() -> {
      byte[] srcBytes = is.readNBytes(length);

      StringBuilder buff = new StringBuilder(length / 2);
      int i = 0;
      while (i < srcBytes.length) {
        byte bWithoutMask = (byte) (srcBytes[i] ^ 128);
        if (isPosibleToBeFirstByteOfDoubleByteString(bWithoutMask)) {
          byte b1WithoutMask = (byte) (srcBytes[i + 1] ^ 128);
          String hexString = HexUtils.toHexString(new byte[] {bWithoutMask, b1WithoutMask});
          if (hexString.startsWith("F")) {
            String gaiji = gaijiMap.get(hexString);
            if (gaiji != null) {
              buff.append(gaiji);
            } else {
              buff.append(hexString);
            }
          } else {
            byte[] bs = new byte[] {bWithoutMask, b1WithoutMask};
            String s = new String(bs, FROM_ENCODE);
            buff.append(s);
          }
          i += 2;
        } else {
          if (bWithoutMask == -96) {
            buff.append(' ');
          } else {
            buff.append(new String(new byte[] {(byte) (bWithoutMask)}, FROM_ENCODE));
          }
          i++;
        }
      }
      return buff.toString().trim();
    }, Try::rethrow);
  }


  private static boolean isPosibleToBeFirstByteOfDoubleByteString(byte b) {
    char c = (char) Byte.toUnsignedInt(b);
    return ((0x81 <= c) && (c <= 0x9f)) || ((0xe0 <= c) && (c <= 0xfc));
  }


  private static String readDoubleByteAsStringAndTrim(FileInputStream is, int length) {
    return Try.getOrElseThrow(() -> new String(is.readNBytes(length), FROM_ENCODE).trim(),
        Try::rethrow);
  }

  private static short readAsShort(FileInputStream is) {
    return Try.getOrElseThrow(() -> {
      ByteBuffer bb = ByteBuffer.allocate(2);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      bb.put(is.readNBytes(2));
      return bb.getShort(0);
    }, Try::rethrow);
  }

  private static class Header {
    public final String title;
    public final int size;
    public final int skip;

    public Header(String title, int size, int skip) {
      this.title = title;
      this.size = size;
      this.skip = skip;
    }

    @Override
    public String toString() {
      return "Header [title=" + title + ", size=" + size + ", skip=" + skip + "]";
    }


  }

}

