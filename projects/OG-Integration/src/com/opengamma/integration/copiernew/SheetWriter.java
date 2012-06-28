package com.opengamma.integration.copiernew;

import com.opengamma.integration.copiernew.rawsheet.writer.RawSheetWriter;
import com.opengamma.integration.copiernew.row.RowWriter;
import com.opengamma.util.ArgumentChecker;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 6/25/12
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class SheetWriter<E> implements Writeable<E> {

  private RowWriter<E> _rowWriter;
  private RawSheetWriter _rawSheetWriter;

  public SheetWriter(RawSheetWriter rawSheetWriter, RowWriter<E> rowWriter) {
    ArgumentChecker.notNull(rawSheetWriter, "rawSheetWriter");
    ArgumentChecker.notNull(rowWriter, "rowWriter");
    _rowWriter = rowWriter;
    _rawSheetWriter = rawSheetWriter;
  }

  @Override
  public E addOrUpdate(E datum) {
    _rawSheetWriter.addOrUpdate(_rowWriter.writeRow(datum));
    return datum;
  }

}
