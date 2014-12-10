package net.sourceforge.seqware.pipeline.modules;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.sourceforge.seqware.common.model.ProcessingAttribute;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.Log;
import net.sourceforge.seqware.common.util.filetools.FileTools;
import net.sourceforge.seqware.pipeline.module.Module;
import net.sourceforge.seqware.pipeline.module.ModuleInterface;
import net.sourceforge.seqware.pipeline.plugins.Metadata;
import org.openide.util.lookup.ServiceProvider;

/**
 * This is a very simple module is used to save both a processing event and 0 or more files to the metadb. It does absolutely no computation
 * at all, it just saves metadata. You might want to use this if your module or call to genericCommandRunner results in a bunch of files
 * being created, each of which need to be associated with different parent objects in the database. You might also use this to combine the
 * outputs of several different steps into a single processing event so the UI in the Portal is simplified.
 * 
 * Here's an example of how you might call the program:
 * 
 * ./bin/seqware-runner.sh --no-metadata --module net.sourceforge.seqware.pipeline.modules.GenericMetadataSaver -- --gms-output-file
 * test:test:/tmp/foo.txt --gms-algorithm foobarAlgorithm
 * 
 * You could, of course, supply metadata db information so the status is written to the database and also a parent accession along with an
 * accession output file to be read in by a subsequent step.
 * 
 * Please use JavaDoc to document each method (the user interface documents will be autogenerated using these comments). See
 * http://en.wikipedia.org/wiki/Javadoc for more information.
 * 
 * @author briandoconnor@gmail.com
 * @version $Id: $Id
 * @deprecated Deprecating this in favour of Metadata(plugin) and the new CLI
 */
@ServiceProvider(service = ModuleInterface.class)
public class GenericMetadataSaver extends Module {

    private OptionSet options = null;
    private ArrayList<String> cmdParameters = null;

    /**
     * getOptionParser is an internal method to parse command line args.
     * 
     * @return OptionParser this is used to get command line options
     */
    @Override
    protected OptionParser getOptionParser() {
        OptionParser parser = new OptionParser();

        parser.accepts(
                "gms-output-file",
                "Specify this option one or more times for each output file created by the command called by this module. The argument is a '::' delimited list of type, meta_type, and file_path.")
                .withRequiredArg().ofType(String.class).describedAs("Optional: <type::meta_type::file_path>");
        parser.accepts("gms-algorithm",
                "You can pass in an algorithm name that will be recorded in the metadb if you are writing back to the metadb.")
                .withRequiredArg().ofType(String.class).describedAs("Required");
        parser.accepts(
                "gms-suppress-output-file-check",
                "If provided, this will suppress checking that the gms-output-file options contain valid file paths. Useful if these are remote resources like HTTP or S3 file URLs.");
        return (parser);
    }

    /**
     * {@inheritDoc}
     * 
     * A method used to return the syntax for this module
     * 
     * @return
     */
    @Override
    public String get_syntax() {
        OptionParser parser = getOptionParser();
        StringWriter output = new StringWriter();
        try {
            parser.printHelpOn(output);
            return (output.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return (e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * The init method is where you put any code needed to setup your module. Here I set some basic information in the ReturnValue object
     * which will eventually populate the "processing" table in seqware_meta_db. I also create a temporary directory using the FileTools
     * object.
     * 
     * init is optional
     * 
     * @return
     */
    @Override
    public ReturnValue init() {

        // setup the return value object, notice that we use
        // ExitStatus, this is what SeqWare uses to track the status
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);
        // fill in the algorithm field in the processing table
        ret.setAlgorithm("GenericMetadataSaver");
        // fill in the description field in the processing table
        ret.setDescription("This is a simple metadata saver.");
        // fill in the version field in the processing table
        ret.setVersion("0.7.0");

        try {

            OptionParser parser = getOptionParser();

            // The parameters object is actually an ArrayList of Strings created
            // by splitting the command line options by space. JOpt expects a String[]

            // an array for this module
            ArrayList<String> myParameters = new ArrayList<>();

            // an array for everything else that will get passed to the command
            cmdParameters = new ArrayList<>();

            // should be able to do this since all the --gms-* params except one take
            // an argument
            for (int i = 0; i < this.getParameters().size(); i++) {
                if (this.getParameters().get(i).startsWith("--gms-")) {
                    myParameters.add(this.getParameters().get(i));
                    if (!this.getParameters().get(i).equals("--gms-suppress-output-file-check") && i + 1 < this.getParameters().size()) {
                        myParameters.add(this.getParameters().get(i + 1));
                        i++;
                    }
                } else {
                    cmdParameters.add(this.getParameters().get(i));
                }
            }

            options = parser.parse(myParameters.toArray(new String[myParameters.size()]));

            // if algo is defined save the new value
            if (options.has("gms-algorithm")) {
                ret.setAlgorithm((String) options.valueOf("gms-algorithm"));
            }

            // you can write to "stdout" or "stderr" which will be persisted back to
            // the DB
            // ret.setStdout(ret.getStdout()+"Output: "+(String)options.valueOf("output-file")+"\n");

        } catch (OptionException e) {
            e.printStackTrace();
            ret.setStderr(e.getMessage());
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }

        // now return the ReturnValue
        return ret;

    }

    /**
     * {@inheritDoc}
     * 
     * Verifies that the parameters make sense
     * 
     * @return
     */
    @Override
    public ReturnValue do_verify_parameters() {

        // most methods return a ReturnValue object
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);

        // now look at the options and make sure they make sense
        for (String option : new String[] { "gms-algorithm" }) {
            if (!options.has(option)) {
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
                String stdErr = ret.getStderr();
                ret.setStderr(stdErr + "Must include parameter: --" + option + "\n");
            }
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     * 
     * The do_verify_input method ensures that the input is reasonable and valid for this tool. For this generic command runner we really
     * can't tell if the
     * 
     * @return
     */
    @Override
    public ReturnValue do_verify_input() {

        // not much to do, let's make sure the
        // temp directory is writable
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);

        // should check the file paths
        if (options.has("gms-output-file") && !options.has("gms-suppress-output-file-check")) {
            List<String> files = (List<String>) options.valuesOf("gms-output-file");
            for (String file : files) {
                String[] tokens = file.split("::");
                if (FileTools.fileExistsAndReadable(new File(tokens[2])).getExitStatus() != ReturnValue.SUCCESS) {
                    Log.error("File does not exist or is not readable: " + tokens[2]);
                    ret.setExitStatus(ReturnValue.FILENOTREADABLE);
                    return ret;
                }
            }
        }

        return ret;

    }

    /**
     * {@inheritDoc}
     * 
     * This is really an optional method but a very good idea. You would test the programs your calling here by running them on a
     * "known good" test dataset and then compare the new answer with the previous known good answer. Other forms of testing could be
     * encapsulated here as well.
     * 
     * @return
     */
    @Override
    public ReturnValue do_test() {

        // notice the use of "NOTIMPLEMENTED", this signifies that we simply
        // aren't doing this step. It's better than just saying SUCCESS
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.NOTIMPLEMENTED);

        // not much to do, just return
        return ret;
    }

    /**
     * {@inheritDoc}
     * 
     * This is the core of a module. It just saves metadata back to the DB using the standardized ReturnValue object as a wrapper.
     * 
     * @return
     */
    @Override
    public ReturnValue do_run() {

        // prepare the return value
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);

        // track the start time of do_run for timing purposes
        ret.setRunStartTstmp(new Date());

        // record the file output
        if (options.has("gms-output-file")) {
            List<String> files = (List<String>) options.valuesOf("gms-output-file");
            for (String file : files) {
                FileMetadata fm = Metadata.fileString2FileMetadata(file);
                ret.getFiles().add(fm);
                if (fm.getMetaType().equals("text/key-value") && this.getProcessingAccession() != 0) {
                    Map<String, String> map = FileTools.getKeyValueFromFile(fm.getFilePath());
                    Set<ProcessingAttribute> atts = new TreeSet<>();
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        ProcessingAttribute a = new ProcessingAttribute();
                        a.setTag(entry.getKey());
                        a.setValue(entry.getValue());
                        atts.add(a);
                    }
                    this.getMetadata().annotateProcessing(this.getProcessingAccession(), atts);
                }
            }
        } else {
            Log.info(get_syntax());
        }

        // note the time do_run finishes
        ret.setRunStopTstmp(new Date());
        return ret;

    }

    /**
     * {@inheritDoc}
     * 
     * A method to check to make sure the output was created correctly
     * 
     * @return
     */
    @Override
    public ReturnValue do_verify_output() {

        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.NOTIMPLEMENTED);

        return ret;

    }

    /**
     * {@inheritDoc}
     * 
     * A cleanup method, make sure you cleanup files that are outside the current working directory since Pegasus won't clean those for you.
     * 
     * clean_up is optional
     * 
     * @return
     */
    @Override
    public ReturnValue clean_up() {

        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.NOTIMPLEMENTED);

        return ret;
    }

}
