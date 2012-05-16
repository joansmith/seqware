/**
 * 
 */
package net.sourceforge.seqware.queryengine.tools.iterators.processors;

import java.util.HashMap;

import net.sourceforge.seqware.queryengine.backend.model.Consequence;
import net.sourceforge.seqware.queryengine.backend.model.Variant;
import net.sourceforge.seqware.queryengine.backend.store.Store;
import net.sourceforge.seqware.queryengine.backend.store.impl.BerkeleyDBStore;
import net.sourceforge.seqware.queryengine.backend.util.iterators.SecondaryCursorIterator;

/**
 * @author boconnor
 * A simple iterator processor that iterates over a set of mismatches and 
 * looks up their consequence entries and dumps a report
 */
public class MismatchConsequenceReportProcessor extends VariantProcessor implements ProcessorInterface {

  // for reporting
  StringBuffer sb = new StringBuffer();

  /**
   * @param outputFilename
   * @param includeIndels
   * @param includeSNVs
   * @param minCoverage
   * @param maxCoverage
   * @param minObservations
   * @param minObservationsPerStrand
   * @param minSNVPhred
   * @param minPercent
   */
  public MismatchConsequenceReportProcessor(String outputFilename, Store store, boolean includeIndels,
      boolean includeSNVs, int minCoverage, int maxCoverage,
      int minObservations, int minObservationsPerStrand, int minSNVPhred,
      int minPercent) {
    super(outputFilename, store, includeIndels, includeSNVs, minCoverage, maxCoverage,
        minObservations, minObservationsPerStrand, minSNVPhred, minPercent);
    // TODO Auto-generated constructor stub
  }
  
  public MismatchConsequenceReportProcessor() { }

  public Object process (Object obj) {

    // the mismatch object
    Variant m = (Variant) obj;

    // first, make sure it passes
    if (m != null && (((m.getType() == m.INSERTION || m.getType() == m.DELETION) && includeIndels) || (m.getType() == m.SNV && includeSNVs)) &&
        m.getReadCount() >= minCoverage && m.getReadCount() <= maxCoverage && m.getCalledBaseCount() >= minObservations &&
        (m.getCalledBaseCountForward() >= minObservationsPerStrand && m.getCalledBaseCountReverse() >= minObservationsPerStrand) &&
        m.getConsensusCallQuality() >= minSNVPhred && m.getZygosity() == m.HOMOZYGOUS) {

      double calledPercent = ((double)m.getCalledBaseCount() / (double)m.getReadCount()) * (double)100.0;
      if (calledPercent >= minPercent) {

        
        
        // then it's met all the filtering criteria
        // now iterate over the consequences
        SecondaryCursorIterator sci = (SecondaryCursorIterator) store.getConsequencesByMismatch(m.getId());
        while(sci.hasNext()) {
          
          Consequence c = (Consequence)sci.next();
          if (c.getTags().keySet().contains("frameshift") || c.getTags().keySet().contains("early-termination") || c.getTags().keySet().contains("intron-splice-site-mutation") || c.getTags().keySet().contains("stop-codon-loss") || c.getTags().keySet().contains("start-codon-loss")) {
            sb.append("Mismatch: "+m.getId()+"\n");
            sb.append("  Info: "+m.getContig()+"\t"+m.getStartPosition()+"\t"+m.getStopPosition()+"\t"+m.getReferenceBase()+"->"+m.getCalledBase()+"\n");
            sb.append("  Consequence:\n");
            for (String tag : c.getTags().keySet()) {
              sb.append("    tag: "+tag+"\n");
            }
            sb.append("    geneID: "+c.getGeneId()+"\n");
          }
        }
        
      }
    }
    return(null);
  }
  
  public String report() {
    
    return(sb.toString());
  }

}
